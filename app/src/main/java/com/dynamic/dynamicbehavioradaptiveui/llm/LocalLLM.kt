package com.dynamic.dynamicbehavioradaptiveui.llm

interface LocalLLM {
    suspend fun generate(request: LLMRequest): LLMResponse
    suspend fun generateStructured(request: LLMRequest): LLMResponse
    fun isAvailable(): Boolean
    fun modelInfo(): ModelInfo
}

data class ModelInfo(
    val name: String,
    val version: String,
    val sizeBytes: Long? = null,
    val parameters: Long? = null,
    val capabilities: List<String> = emptyList()
)