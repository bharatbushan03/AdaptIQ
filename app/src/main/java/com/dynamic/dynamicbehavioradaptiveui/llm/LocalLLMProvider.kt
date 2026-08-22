package com.dynamic.dynamicbehavioradaptiveui.llm

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.timeout

abstract class LocalLLMProvider(
    private val defaultTimeoutMs: Int = 10000
) : LocalLLM {

    override suspend fun generate(request: LLMRequest): LLMResponse {
        return withContext(Dispatchers.Default) {
            try {
                timeout(defaultTimeoutMs) {
                    generateInternal(request)
                }
            } catch (e: java.util.concurrent.TimeoutException) {
                LLMResponse(
                    text = "",
                    success = false,
                    error = "LLM inference timed out after ${defaultTimeoutMs}ms"
                )
            } catch (e: Exception) {
                LLMResponse(
                    text = "",
                    success = false,
                    error = "LLM inference failed: ${e.message}"
                )
            }
        }
    }

    abstract suspend fun generateInternal(request: LLMRequest): LLMResponse

    override suspend fun generateStructured(request: LLMRequest): LLMResponse {
        return withContext(Dispatchers.Default) {
            try {
                timeout(defaultTimeoutMs) {
                    generateStructuredInternal(request)
                }
            } catch (e: java.util.concurrent.TimeoutException) {
                LLMResponse(
                    text = "",
                    success = false,
                    error = "Structured LLM inference timed out after ${defaultTimeoutMs}ms"
                )
            } catch (e: Exception) {
                LLMResponse(
                    text = "",
                    success = false,
                    error = "Structured LLM inference failed: ${e.message}"
                )
            }
        }
    }

    abstract suspend fun generateStructuredInternal(request: LLMRequest): LLMResponse

    override fun isAvailable(): Boolean {
        try {
            val info = modelInfo()
            return info.name.isNotEmpty() && info.capabilities.isNotEmpty()
        } catch {
            return false
        }
    }

    override fun modelInfo(): ModelInfo {
        return try {
            generateModelInfoInternal()
        } catch {
            ModelInfo(name = "unknown", version = "0.0.1", capabilities = emptyList())
        }
    }

    abstract suspend fun generateModelInfoInternal(): ModelInfo
}