package com.dynamic.dynamicbehavioradaptiveui.llm

import com.dynamic.dynamicbehavioradaptiveui.models.AdaptationRecommendation
import com.dynamic.dynamicbehavioradaptiveui.models.SafetyLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.cancelable
import kotlin.concurrent.timeout

interface LocalLLM {

    data class ModelInfo(
        name: String,
        sizeKb: Int,
        inputDimensions: Pair<Int, Int>,
        outputDimensions: Int,
        supportsGPU: Boolean
    )

    data class LLMRequest(
        prompt: String,
        temperature: Float = 0.3f,
        maxTokens: Int = 256,
        structured: Boolean = false
    )

    data class LLMResponse(
        text: String,
        tokensUsed: Int,
        latencyMs: Long,
        model: String
    )

    suspend fun generate(request: LLMRequest): LLMResponse

    suspend fun generateStructured(
        prompt: String,
        schema: String,
        temperature: Float = 0.3f,
        maxTokens: Int = 256
    ): AdaptationRecommendation?

    val modelInfo: ModelInfo

    val isAvailable: Boolean

    fun invalidateCache()
}