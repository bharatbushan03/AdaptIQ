package com.dynamic.dynamicbehavioradaptiveui.llm

// Mock local LLM placeholder - no actual AI implementation yet
class LocalLLM {
    suspend fun generateResponse(prompt: String): String {
        return "Mock response for: $prompt"
    }
}