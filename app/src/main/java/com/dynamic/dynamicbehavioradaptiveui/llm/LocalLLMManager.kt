package com.dynamic.dynamicbehavioradaptiveui.llm

import com.dynamic.dynamicbehavioradaptiveui.behavior.BehaviorStateEngine
import com.dynamic.dynamicbehavioradaptiveui.behavior.InteractionFriction
import com.dynamic.dynamicbehavioradaptiveui.models.BehaviorState

class LocalLLMManager(
    private val localLLM: LocalLLM,
    private val fallbackTimeoutMs: Int = 5000
) {

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

    private fun fallbackToBehaviorState(operation: String, request: LLMRequest): LLMResponse {
        val engine = BehaviorStateEngine()
        val state = engine.getCurrentState("default")
        val text = when (state.interactionFriction) {
            InteractionFriction.HIGH -> "Adaptation: Reduce UI complexity, provide simplified controls and delayed responses due to high interaction friction detected."
            InteractionFriction.MEDIUM -> "Adaptation: Maintain current UI flow with subtle progressive hints based on user behavior patterns."
            InteractionFriction.LOW -> "Adaptation: Introduce advanced features gradually, as low friction indicates user proficiency."
            else -> "Adaptation: Apply default behavioral interpretation."
        }
        return LLMResponse(
            text = text,
            success = true,
            error = null,
            model = "BehaviorStateEnginefallback"
        )
    }
}