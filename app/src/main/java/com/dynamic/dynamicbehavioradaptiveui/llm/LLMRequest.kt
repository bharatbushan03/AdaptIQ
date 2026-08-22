package com.dynamic.dynamicbehavioradaptiveui.llm

data class LLMRequest(
    val prompt: String,
    val temperature: Float = 0.7f,
    val maxTokens: Int = 512,
    val stop: List<String> = emptyList(),
    val schema: Any? = null,
    val timeoutMs: Int = 10000
)