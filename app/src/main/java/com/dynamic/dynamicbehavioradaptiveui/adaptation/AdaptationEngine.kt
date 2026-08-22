package com.dynamic.dynamicbehavioradaptiveui.adaptation

import com.dynamic.dynamicbehavioradaptiveui.behavior.BehaviorTracker
import com.dynamic.dynamicbehavioradaptiveui.models.*
import com.dynamic.dynamicbehavioradaptiveui.llm.LocalLLM

class AdaptationEngine(
    private val behaviorTracker: BehaviorTracker,
    private val localLLM: LocalLLM,
    private val scorer: AdaptationScorer = AdaptationScorer()
) {

    suspend fun adaptUI(
        sessionId: String,
        request: AdaptationRequest,
        preAdaptationMetrics: AdaptationMetrics,
        preTaskCompletionTime: Long?
    ): AdaptationOutcome? {
        val currentState = behaviorTracker.getCurrentState(sessionId)

        val adaptation = generateAdaptation(request, currentState)
        if (adaptation == null) return null

        if (!PolicyValidator.create().validateRecommendation(request, adaptation)) {
            return AdaptationOutcome(
                adaptationId = adaptation.id,
                triggerReason = "policy_rejection",
                uiChange = adaptation.description,
                confidence = adaptation.confidence,
                sessionId = sessionId,
                preAdaptationMetrics = preAdaptationMetrics,
                postAdaptationMetrics = null,
                taskCompletionTimeBefore = preTaskCompletionTime,
                taskCompletionTimeAfter = null,
                taskCompletedAfter = false,
                userInteractionsAfter = emptyList(),
                outcome = AdaptationOutcomeDetermination.EXPIRE,
                effectivenessScore = 0.0
            )
        }

val adaptedState = adaptation.applyFn(AppState(
            sessionId = sessionId,
            activeAdaptations = listOf(adaptation),
            metrics = preAdaptationMetrics
        ))

        val outcome = recordAdaptationOutcome(
            sessionId = sessionId,
            adaptation = adaptation,
            preMetrics = preAdaptationMetrics,
            postMetrics = adaptedState.metrics,
            preTaskCompletionTime = preTaskCompletionTime,
            postTaskCompletionTime = adaptedState.taskCompletionTime,
            userInteractions = adaptedState.interactions,
            taskCompletedAfter = adaptedState.taskCompleted
        )

        behaviorTracker.trackAdaptation(adaptation, adaptedState)

        return outcome
    }

    private fun generateAdaptation(
        request: AdaptationRequest,
        currentState: BehaviorState
    ): Adaptation? {
        return when (request.currentBehaviorState.currentIntent) {
            "repeated_workflow" -> Adaptation(
                `type` = AdaptationType.NAVIGATION_ENHANCEMENT,
                description = "Quick access to frequent workflows",
                applyFn = { appState ->
                    appState.copy(
                        primaryNavigation = "bottom_bar_with_quick_actions",
                        criticalControls = appState.criticalControls + "quick_action"
                    )
                },
                revertFn = { appState ->
                    appState.copy(
                        primaryNavigation = "bottom_bar",
                        criticalControls = appState.criticalControls - "quick_action"
                    )
                },
                confidence = 0.7,
                expiresAt = System.currentTimeMillis() + 24 * 60 * 60 * 1000,
                isReversible = true,
                isSecuritySensitive = false
            )
            "exploring_features" -> Adaptation(
                `type` = AdaptationType.FEATURE_VISIBILITY,
                description = "Highlight frequently used features",
                applyFn = { appState ->
                    appState.copy(
                        criticalControls = appState.criticalControls + "feature_highlight"
                    )
                },
                revertFn = { appState ->
                    appState.copy(
                        criticalControls = appState.criticalControls - "feature_highlight"
                    )
                },
                confidence = 0.6,
                expiresAt = System.currentTimeMillis() + 24 * 60 * 60 * 1000,
                isReversible = true,
                isSecuritySensitive = false
            )
            "in_progress_workflow" -> Adaptation(
                `type` = AdaptationType.WIDGET_REORDERING,
                description = "Prioritize workflow-related widgets",
                applyFn = { appState ->
                    appState.copy(
                        criticalControls = appState.criticalControls + "workflow_widget"
                    )
                },
                revertFn = { appState ->
                    appState.copy(
                        criticalControls = appState.criticalControls - "workflow_widget"
                    )
                },
                confidence = 0.65,
                expiresAt = System.currentTimeMillis() + 24 * 60 * 60 * 1000,
                isReversible = true,
                isSecuritySensitive = false
            )
            else -> null
        }
    }

    private fun recordAdaptationOutcome(
        sessionId: String,
        adaptation: Adaptation,
        preMetrics: AdaptationMetrics,
        postMetrics: AdaptationMetrics,
        preTaskCompletionTime: Long?,
        postTaskCompletionTime: Long?,
        userInteractions: List<InteractionEvent>,
        taskCompletedAfter: Boolean
    ): AdaptationOutcome {
        val adaptationAccepted = userInteractions.any { it.metadata.contains("adaptation_accepted") }

        val outcome = scorer.scoreOutcome(
            preMetrics = preMetrics,
            postMetrics = postMetrics,
            preTaskCompletionTime = preTaskCompletionTime,
            postTaskCompletionTime = postTaskCompletionTime,
            taskCompletedAfter = taskCompletedAfter,
            userInteractionsAfter = userInteractions.size,
            adaptationAccepted = adaptationAccepted
        )

        val outcomeObj = AdaptationOutcome(
            adaptationId = adaptation.id,
            triggerReason = adaptation.description,
            uiChange = adaptation.description,
            confidence = adaptation.confidence,
            sessionId = sessionId,
            preAdaptationMetrics = preMetrics,
            postAdaptationMetrics = postMetrics,
            taskCompletionTimeBefore = preTaskCompletionTime,
            taskCompletionTimeAfter = postTaskCompletionTime,
            taskCompletedAfter = taskCompletedAfter,
            userInteractionsAfter = userInteractions,
            outcome = outcome,
            effectivenessScore = scorer.calculateEffectivenessScore(postMetrics)
        )

        behaviorTracker.recordAdaptationOutcome(outcomeObj)
        return outcomeObj
    }
}