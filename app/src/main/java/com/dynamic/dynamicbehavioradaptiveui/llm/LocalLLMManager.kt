package com.dynamic.dynamicbehavioradaptiveui.llm

import com.dynamic.dynamicbehavioradaptiveui.behavior.BehaviorStateEngine
import com.dynamic.dynamicbehavioradaptiveui.llm.AdaptationValidator.*
import org.json.JSONObject
import java.time.Instant

class LocalLLMManager(
    private val localLLM: LocalLLM,
    private val fallbackTimeoutMs: Int = 5000
) {

    data class AdaptationPromptData(
        val recentInteractionSequence: String,
        val behavioralProfile: String,
        val currentBehaviorState: String,
        val currentScreen: String,
        val availableAdaptiveUIComponents: String,
        val previousAdaptationOutcomes: String
    )

    suspend fun generate(request: LLMRequest): LLMResponse {
        if (!localLLM.isAvailable()) {
            return fallbackToBehaviorState("generate", request)
        }
        try {
            return withTimeout(fallbackTimeoutMs) {
                localLLM.generate(request)
            }
        } catch (e: java.util.concurrent.TimeoutException) {
            return fallbackToBehaviorState("generate timeout", request)
        } catch (e: Exception) {
            return fallbackToBehaviorState("generate error: ${e.message}", request)
        }
    }

    suspend fun generateStructured(request: LLMRequest): LLMResponse {
        if (!localLLM.isAvailable()) {
            return fallbackToBehaviorState("generateStructured unavailable", request)
        }
        try {
            return withTimeout(fallbackTimeoutMs) {
                localLLM.generateStructured(request)
            }
        } catch (e: java.util.concurrent.TimeoutException) {
            return fallbackToBehaviorState("generateStructured timeout", request)
        } catch (e: Exception) {
            return fallbackToBehaviorState("generateStructured error: ${e.message}", request)
        }
    }

    private fun <T> parseAndValidateAdaptation(json: T): AdaptationRecommendation? {
        return when (json) {
            is String -> AdaptationValidator.parseRecommendation(json)
            is JSONObject -> {
                val result = AdaptationValidator.parseRecommendation(json.toString())
                result
            }
            else -> null
        }
    }

    suspend fun generateAdaptationRecommendation(
        promptData: AdaptationPromptData
    ): AdaptationRecommendation? {
        val prompt = buildAdaptationPrompt(promptData)
        val request = LLMRequest(
            prompt = prompt,
            temperature = 0.3f,
            maxTokens = 256,
            schema = AdaptationJsonSchema.SCHEMA,
            timeoutMs = 10000
        )

        val response = generateStructured(request)

        if (response.error != null || response.text.isBlank()) {
            return fallbackToBehaviorState(promptData)
        }

        val parsed = parseAndValidateAdaptation(response.text)

        if (parsed != null && AdaptationValidator.validateRecommendation(parsed)) {
            if (AdaptationValidator.checkSafetyConstraints(parsed, null, null)) {
                return parsed
            }
        }

        return fallbackToBehaviorState(promptData)
    }

    private fun buildAdaptationPrompt(data: AdaptationPromptData): String {
        return """
            |You are an adaptive UI optimization assistant for an Android application. 
            |Based on the following context, recommend exactly one adaptation action.
            |
            |Recent interaction sequence: ${data.recentInteractionSequence}
            |Behavioral profile: ${data.behavioralProfile}
            |Current behavior state: ${data.currentBehaviorState}
            |Current screen: ${data.currentScreen}
            |Available adaptive UI components: ${data.availableAdaptiveUIComponents}
            |Previous adaptation outcomes: ${data.previousAdaptationOutcomes}
            |
            |Return ONLY a valid JSON object with exactly these fields:
            |- action: one of SHOW_SHORTCUT, HIDE_LOW_PRIORITY_ACTION, SHOW_GUIDANCE, REORDER_SECONDARY_ACTIONS, REDUCE_INFORMATION_DENSITY, INCREASE_INFORMATION_DENSITY, HIGHLIGHT_RELEVANT_ACTION, NO_CHANGE
            |- target: string (the UI element or screen area the action applies to)
            |- reason: string (why this adaptation is recommended)
            |- confidence: number (0.0 to 1.0)
            |- expectedBenefit: string (the expected benefit of this adaptation)
            |- expiration: integer (milliseconds until adaptation expires, must be positive)
            |- safetyLevel: string (low, medium, or high)
            |
            |Do NOT include any other text, explanation, or formatting. Return only the JSON object.
            |Do NOT attempt to change primary navigation, remove critical controls, modify permissions, 
            |change security settings, execute arbitrary code, or access external services.
            |""".trimMargin()
    }

    private fun fallbackToBehaviorState(promptData: AdaptationPromptData): AdaptationRecommendation? {
        val engine = BehaviorStateEngine()
        val state = engine.getCurrentState("default")
        val friction = state.interactionFriction

        return when (friction) {
            InteractionFriction.HIGH -> AdaptationRecommendation(
                action = AdaptationAction.REDUCE_INFORMATION_DENSITY,
                target = "main_screen",
                reason = "High interaction friction detected, reducing information density improves usability",
                confidence = 0.85f,
                expectedBenefit = "Improved readability and reduced cognitive load",
                expiration = Instant.now().toEpochMilli() + 300000,
                safetyLevel = SafetyLevel.MEDIUM
            )
            InteractionFriction.MEDIUM -> AdaptationRecommendation(
                action = AdaptationAction.SHOW_GUIDANCE,
                target = "main_screen",
                reason = "Medium interaction friction, providing subtle progressive hints",
                confidence = 0.75f,
                expectedBenefit = "Guided workflow progression without overwhelming the user",
                expiration = Instant.now().toEpochMilli() + 600000,
                safetyLevel = SafetyLevel.LOW
            )
            InteractionFriction.LOW -> AdaptationRecommendation(
                action = AdaptationAction.SHOW_SHORTCUT,
                target = "main_screen",
                reason = "Low interaction friction indicates user proficiency, showing relevant shortcuts",
                confidence = 0.8f,
                expectedBenefit = "Faster task completion through shortcuts",
                expiration = Instant.now().toEpochMilli() + 600000,
                safetyLevel = SafetyLevel.LOW
            )
            else -> null
        }
    }
}