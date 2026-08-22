package com.dynamic.dynamicbehavioradaptiveui.adaptation

import com.dynamic.dynamicbehavioradaptiveui.adaptation.PolicyValidator
import com.dynamic.dynamicbehavioradaptiveui.behavior.BehaviorFeatureExtractor
import com.dynamic.dynamicbehavioradaptiveui.behavior.BehaviorStateEngine
import com.dynamic.dynamicbehavioradaptiveui.behavior.InteractionEvent
import com.dynamic.dynamicbehavioradaptiveui.models.BehaviorState
import com.dynamic.dynamicbehavioradaptiveui.behavior.ColdStartPhase
import com.dynamic.dynamicbehavioradaptiveui.models.ProficiencyLevel
import com.dynamic.dynamicbehavioradaptiveui.models.InteractionFriction
import com.dynamic.dynamicbehavioradaptiveui.adaptation.AdaptationType
import com.dynamic.dynamicbehavioradaptiveui.llm.LocalLLM
import com.dynamic.dynamicbehavioradaptiveui.llm.LLMRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AdaptiveUIEngine(
    private val behaviorFeatureExtractor: BehaviorFeatureExtractor,
    private val behaviorStateEngine: BehaviorStateEngine,
    private val policyValidator: PolicyValidator = PolicyValidator(),
    private val localLLM: LocalLLM
) {

    suspend fun observeAndAdapt(sessionId: String, events: List<InteractionEvent>): AdaptiveUIState {
        return withContext(Dispatchers.Main) {
            adapt(sessionId, events)
        }
    }

    private suspend fun adapt(sessionId: String, events: List<InteractionEvent>): AdaptiveUIState {
        val currentState = behaviorStateEngine.getCurrentState(sessionId)
        val phase = behaviorStateEngine.getCurrentAdaptationPhase(sessionId)

        val adaptationPhase = when (phase.phase) {
            ColdStartPhase.PHASE_1_STABLE_DEFAULT -> AdaptiveUIState(
                behaviorState = currentState,
                currentAppState = AppState(sessionId = sessionId),
                isAdapting = false
            )
            ColdStartPhase.PHASE_2_CONSERVATIVE_SHORTCUTS -> {
                val conservativeAdaptation = applyConservativeShortcuts(currentState)
                AdaptiveUIState(
                    behaviorState = currentState,
                    currentAppState = conservativeAdaptation.appState,
                    isAdapting = false,
                    pendingAdaptation = conservativeAdaptation.pendingAdaptation
                )
            }
            ColdStartPhase.PHASE_3_PERSONALIZED_ADAPTIVE -> {
                val request = AdaptationRequest(
                    sessionId = sessionId,
                    currentBehaviorState = currentState,
                    interactionEvents = events
                )

                if (shouldRequestLLM(request, currentState)) {
                    adaptWithLLM(request, currentState, events)
                } else {
                    AdaptiveUIState(
                        behaviorState = currentState,
                        currentAppState = AppState(sessionId = sessionId),
                        isAdapting = false
                    )
                }
            }
        }

        return adaptationPhase
    }

    private fun shouldRequestLLM(request: AdaptationRequest, currentState: BehaviorState): Boolean {
        val hasSignificantChanges = hasBehavioralChanges(request)
        val notAlreadyAdapting = request.interactionEvents.isNotEmpty()
        return hasSignificantChanges && notAlreadyAdapting
    }

    private fun hasBehavioralChanges(request: AdaptationRequest): Boolean {
        val recentEvents = request.interactionEvents.takeLast(20)
        if (recentEvents.isEmpty()) return false

        val featureCounts = recentEvents
            .map { it.metadata }
            .filter { it.isNotEmpty() }
            .flatten()

        return featureCounts.size >= 3
    }

    private suspend fun adaptWithLLM(
        request: AdaptationRequest,
        currentState: BehaviorState,
        events: List<InteractionEvent>
    ): AdaptiveUIState {
        return withContext(Dispatchers.Main) {
            val recommendation = requestLocalLLM(request, currentState, events)
            val validated = policyValidator.validateRecommendation(request, recommendation)

            if (!validated) {
                return AdaptiveUIState(
                    behaviorState = currentState,
                    currentAppState = AppState(sessionId = request.sessionId),
                    pendingAdaptation = null
                )
            }

            val adaptation = createAdaptationFromRecommendation(recommendation, currentState)
            val appState = AppState(sessionId = request.sessionId)

            val canApply = policyValidator.validateCanApply(appState, adaptation)

            if (!canApply) {
                return AdaptiveUIState(
                    behaviorState = currentState,
                    currentAppState = appState,
                    pendingAdaptation = adaptation
                )
            }

            val result = applyAdaptation(adaptation, appState)

            AdaptiveUIState(
                behaviorState = currentState,
                currentAppState = appState.copy(
                    activeAdaptations = result.postState?.activeAdaptations ?: emptyList()
                ),
                pendingAdaptation = null,
                adaptationHistory = result +: request.interactionEvents
                    .mapNotNull { event ->
                        when (event.metadata) {
                            is String -> if (event.metadata.startsWith("adaptation_")) {
                                event.metadata.takeWhile { it != '\n' }
                            } else null
                            else null
                        }
                    }
                    .filter { it.isNotEmpty() }
                    .takeLast(5),
                isAdapting = true,
                lastAdaptationTime = System.currentTimeMillis()
            )
        }
    }

    private suspend fun requestLocalLLM(
        request: AdaptationRequest,
        currentState: BehaviorState,
        events: List<InteractionEvent>
    ): Adaptation {
        val prompt = generateLLMPrompt(request, currentState)

        val llmRequest = LLMRequest(
            prompt = prompt,
            temperature = 0.3f,
            maxTokens = 256,
            schema = AdaptationRequest::class.java,
            timeoutMs = 8000
        )

        val response = withContext(Dispatchers.Default) {
            localLLM.generateStructured(llmRequest)
        }

        return if (response.success && response.text.isNotEmpty()) {
            parseAdaptationFromResponse(response.text, currentState)
        } else {
            createFallbackAdaptation(currentState)
        }
    }

    private fun generateLLMPrompt(
        request: AdaptationRequest,
        currentState: BehaviorState
    ): String {
        val behaviorSummary = """
            Current behavior state:
            - Proficiency: ${currentState.proficiencyLevel}
            - Friction: ${currentState.interactionFriction}
            - Intent: ${currentState.currentIntent}
            - Workflow familiarity: ${currentState.workflowFamiliarity}

            Recent interaction events count: ${request.interactionEvents.size}

            Recommend an adaptation type (LAYOUT_ADJUSTMENT, THEME_CUSTOMIZATION, 
            FEATURE_VISIBILITY, NAVIGATION_ENHANCEMENT, WIDGET_REORDERING) that:
            - Is reversible
            - Is not security-sensitive
            - Does not affect primary navigation
            - Does not affect critical controls
            - Has confidence >= 0.6
            """.trimIndent()

        return prompt + "\n\n" + behaviorSummary
    }

    private fun parseAdaptationFromResponse(
        responseText: String,
        currentState: BehaviorState
    ): Adaptation {
        val lower = responseText.lowercase()

        val adaptationType = when {
            lower.contains("layout") -> AdaptationType.LAYOUT_ADJUSTMENT
            lower.contains("theme") -> AdaptationType.THEME_CUSTOMIZATION
            lower.contains("visibility") -> AdaptationType.FEATURE_VISIBILITY
            lower.contains("navigation") -> AdaptationType.NAVIGATION_ENHANCEMENT
            lower.contains("widget") -> AdaptationType.WIDGET_REORDERING
            else -> AdaptationType.LAYOUT_ADJUSTMENT
        }

        val isReversible = lower.contains("reversible") || lower.contains("undo")
        val isSecuritySensitive = lower.contains("security") || lower.contains("auth") || lower.contains("password")

        val description = "Adaptation based on behavior analysis: $responseText"

        return Adaptation(
            id = java.util.UUID.randomUUID().toString(),
            `type` = adaptationType,
            description = description,
            applyFn = { appState ->
                appState.copy(
                    activeAdaptations = adaptationOfType(appState.activeAdaptations, adaptationType) + adaptation
                )
            },
            revertFn = { appState ->
                appState.copy(
                    activeAdaptations = appState.activeAdaptations.filter { it.`type` != adaptationType }
                )
            },
            confidence = 0.7,
            expiresAt = System.currentTimeMillis() + 300000,
            isReversible = isReversible,
            isSecuritySensitive = isSecuritySensitive
        )
    }

    private fun createFallbackAdaptation(currentState: BehaviorState): Adaptation {
        return Adaptation(
            id = java.util.UUID.randomUUID().toString(),
            `type` = AdaptationType.LAYOUT_ADJUSTMENT,
            description = "Fallback adaptation - no LLM recommendation, applying safe default",
            applyFn = { appState -> appState },
            revertFn = { appState -> appState },
            confidence = 0.0,
            expiresAt = System.currentTimeMillis() + 60000,
            isReversible = true,
            isSecuritySensitive = false
        )
    }

    private fun createAdaptationFromRecommendation(
        recommendation: Adaptation,
        currentState: BehaviorState
    ): Adaptation {
        return Adaptation(
            id = java.util.UUID.randomUUID().toString(),
            `type` = recommendation.`type`,
            description = recommendation.description,
            applyFn = recommendation.applyFn,
            revertFn = recommendation.revertFn,
            confidence = recommendation.confidence,
            expiresAt = recommendation.expiresAt,
            isReversible = recommendation.isReversible,
            isSecuritySensitive = recommendation.isSecuritySensitive
        )
    }

    private fun applyAdaptation(
        adaptation: Adaptation,
        appState: AppState
    ): AdaptationResult {
        try {
            val newState = adaptation.applyFn(appState)
            return AdaptationResult(
                adaptation = adaptation,
                success = true,
                message = "Adaptation applied successfully: ${adaptation.type}",
                preState = appState,
                postState = newState
            )
        } catch (e: Exception) {
            return AdaptationResult(
                adaptation = adaptation,
                success = false,
                message = "Failed to apply adaptation: ${e.message}",
                preState = appState,
                postState = null
            )
        }
    }

    private fun adaptationOfType(
        adaptations: List<Adaptation>,
        targetType: AdaptationType
    ): List<Adaptation> {
        return adaptations.filter { it.`type` != targetType }
    }

    fun trackInteractionEvent(sessionId: String, event: InteractionEvent): AdaptiveUIState {
        val current = behaviorStateEngine.getCurrentState(sessionId)
        return AdaptiveUIState(
            behaviorState = current
        )
    }

    private fun applyConservativeShortcuts(currentState: BehaviorState): AdaptiveUIState {
        val showTooltips = currentState.proficiencyLevel == ProficiencyLevel.BEGINNER
        val simplifiedLayout = showTooltips
        val reduceDensity = currentState.interactionFriction == InteractionFriction.HIGH
        val hideSecondary = reduceDensity

        val adaptation = Adaptation(
            id = java.util.UUID.randomUUID().toString(),
            `type` = AdaptationType.LAYOUT_ADJUSTMENT,
            description = "Conservative contextual shortcuts for emerging user behavior",
            applyFn = { appState ->
                appState.copy(activeAdaptations = emptyList())
            },
            revertFn = { appState -> appState },
            confidence = 0.4,
            expiresAt = System.currentTimeMillis() + 300000,
            isReversible = true,
            isSecuritySensitive = false
        )

        return AdaptiveUIState(
            behaviorState = currentState,
            currentAppState = AppState(sessionId = currentState.toString()),
            pendingAdaptation = adaptation,
            isAdapting = true
        )
    }