package com.dynamic.dynamicbehavioradaptiveui.llm

import com.dynamic.dynamicbehavioradaptiveui.models.AdaptationRecommendation
import com.dynamic.dynamicbehavioradaptiveui.models.SafetyLevel
import androidx.annotation.MainThread
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.Interpreter.Options
import org.tensorflow.lite.gpu.GpuDelegate
import java.io.File
import java.nio.MappedByteBuffer
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CancelHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

class TFLiteLocalLLMProvider(modelAssetPath: String, useGPU: Boolean = true) : LocalLLM {

    private val interpreter: Interpreter
    private val modelInfo = ModelInfo(
        name = "tflite-model",
        sizeKb = 5000,
        inputDimensions = Pair(1, 512),
        outputDimensions = 10,
        supportsGPU = useGPU
    )

    private val behaviorStateCache = ConcurrentHashMap<String, Any>()
    private val llmDecisionCache = ConcurrentHashMap<String, AdaptationRecommendation>()
    private val inferenceSemaphore = Semaphore(4)
    private val debounceTimer = DelayedHandler()
    private val inferenceBatch = MutableList<LLMRequest>()
    private var batchFlushJob: Job? = null

    init {
        val modelFile = File(modelAssetPath)
        val fileMappedBuffer = FileInputStream(modelFile).channel.map(FileChannel.MapMode.READ_ONLY, 0, modelFile.length())
        val options = Options()
        if (useGPU) {
            val gpuDelegate = GpuDelegate()
            options.addDelegate(gpuDelegate)
        }
        interpreter = Interpreter(fileMappedBuffer, options)
    }

    override suspend fun generate(request: LLMRequest): LLMResponse {
        return withContext(Dispatchers.Default) {
            measureTimeMillis {
                inferWithTimeout(request)
            }
        }.also { latency ->
            // Cache the result for debounce/batching
            behaviorStateCache[request.prompt.hashCode() % 1000] = it
        }
    }

    private fun inferWithTimeout(request: LLMRequest): LLMResponse {
        return try {
            inferenceSemaphore.acquire()
            val startTime = System.currentTimeMillis()
            val outputBuffer = ByteBuffer.allocateDirect(modelInfo.outputDimensions * 4)
            val inputBuffer = ByteBuffer.allocateDirect(modelInfo.inputDimensions.first * modelInfo.inputDimensions.second * 4)

            // Prepare input from request prompt
            prepareInput(request.prompt, inputBuffer)

            // Run inference
            interpreter.run(inputBuffer, outputBuffer, null)

            val latencyMs = System.currentTimeMillis() - startTime
            val generatedText = extractOutput(outputBuffer)

            LLMResponse(
                text = generatedText,
                tokensUsed = inputBuffer.remaining() / 4,
                latencyMs = latencyMs,
                model = modelInfo.name
            )
        } catch (e: Exception) {
            throw RuntimeException("LLM inference failed: ${e.message}", e)
        } finally {
            inferenceSemaphore.release()
        }
    }

    private fun prepareInput(prompt: String, inputBuffer: ByteBuffer) {
        // Tokenize and pad input to model's expected dimensions
        val tokens = prompt.codePoints
        val maxLen = modelInfo.inputDimensions.first * modelInfo.inputDimensions.second
        val padded = tokens.take(maxLen)
        val floatArray = padded.floatToByte()
        inputBuffer.put(floatArray)
    }

    private fun extractOutput(outputBuffer: ByteBuffer): String {
        // Decode output bytes to string
        val bytes = outputBuffer.array()
        String(bytes).trim()
    }

    override suspend fun generateStructured(
        prompt: String,
        schema: String,
        temperature: Float,
        maxTokens: Int
    ): AdaptationRecommendation? {
        // Check cache first
        val cacheKey = "${prompt.hashCode()}_${schema}_${temperature}"
        if (llmDecisionCache.containsKey(cacheKey)) {
            return llmDecisionCache[cacheKey]!!
        }

        // Run inference and structure result
        val response = generate(LLMRequest(prompt = prompt, temperature = temperature, maxTokens = maxTokens, structured = true))

        val recommendation = if (response.text.isNotEmpty()) {
            AdaptationRecommendation(
                action = AdaptationAction.NAVIGATE,
                confidence = 0.7f,
                safetyLevel = SafetyLevel.MEDIUM,
                rationale = response.text,
                requiredFields = mapOf()
            )
        } else {
            null
        }

        // Cache the decision
        llmDecisionCache[cacheKey] = recommendation
        return recommendation
    }

    override val modelInfo: ModelInfo
        get() = modelInfo

    override val isAvailable: Boolean
        get() = interpreter != null

    override fun invalidateCache() {
        behaviorStateCache.clear()
        llmDecisionCache.clear()
        debounceTimer.cancel()
        inferenceBatch.clear()
    }

    fun scheduleInference(request: LLMRequest, handler: (LLMResponse) -> Unit) {
        // Debounce: cancel any pending inference for similar prompt
        debounceTimer.reset { -> executeBatchedInference(request, handler) }
    }

    private fun executeBatchedInference(request: LLMRequest, handler: (LLMResponse) -> Unit) {
        withContext(Dispatchers.Default) {
            // Add to batch and flush after short delay
            inferenceBatch.add(request)
            if (inferenceBatch.size >= 3) {
                flushBatch(handler)
            } else {
                // Schedule single flush after debounce period
                debounceTimer.reset { flushBatch(handler) }
            }
        }
    }

    private fun flushBatch(handler: (LLMResponse) -> Unit) {
        if (inferenceBatch.isEmpty()) return

        val batch = inferenceBatch.take()
        // Run inferences sequentially but on background thread
        val responses = batch.mapIndexed { index, req ->
            try {
                inferWithTimeout(req)
            } catch (e: Exception) {
                LLMResponse(text = "", tokensUsed = 0, latencyMs = 0, model = modelInfo.name)
            }
        }

        if (responses.isNotEmpty()) {
            handler(responses[0])
        }
        inferenceBatch.clear()
    }

    fun cancelCurrentInference(): Boolean {
        debounceTimer.cancel()
        inferenceBatch.clear()
        return true
    }

    inner class DelayedHandler(
        private val delayMs: Long = 200
    ) {
        private var pendingJob: Job? = null

        fun reset(run: () -> Unit) {
            pendingJob?.cancel()
            pendingJob = kotlinx.coroutines.launch(Dispatchers.Main) {
                kotlinx.coroutines.delay(delayMs)
                run()
            }
        }

        fun cancel() {
            pendingJob?.cancel()
            pendingJob = null
        }
    }
}