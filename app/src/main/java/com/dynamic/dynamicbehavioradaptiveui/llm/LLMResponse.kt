package com.dynamic.dynamicbehavioradaptiveui.llm

data class LLMResponse(
    val text: String,
    val success: Boolean = true,
    val usage: LLMUsage? = null,
    val error: String? = null,
    val model: String? = null,
    val finishedAt: Long = System.currentTimeMillis()
)

data class LLMUsage(
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int
)