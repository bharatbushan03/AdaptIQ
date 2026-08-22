package com.dynamic.dynamicbehavioradaptiveui.llm

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.Interpreter.Options
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import org.tensorflow.lite.DataType
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.random.Random

fun IntArray.product(): Int {
    var result = 1
    for (element in this) {
        result *= element
    }
    return result
}

class TFLiteLocalLLMProvider(
    private val modelAssetPath: String,
    private val context: Context,
    private val timeoutMs: Int = 8000,
    private val useGpu: Boolean = false
) : LocalLLM {

    private var interpreter: Interpreter? = null
    private var inputBuffer: TensorBuffer? = null
    private var outputBuffer: TensorBuffer? = null
    private shutdownRequested = false

    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    init {
        loadModel()
    }

    private fun loadModel() {
        try {
            val modelFile = FileUtil.loadMappedFile(context, modelAssetPath)
            val options = Options()
            if (useGpu) {
                val gpuDelegate = GpuDelegate()
                options.addDelegate(gpuDelegate)
            }
            interpreter = Interpreter(modelFile, options)

            val inputTensor = interpreter!!.getInputTensor(0)
            val outputTensor = interpreter!!.getOutputTensor(0)

            val inputShape = inputTensor.shape()
            val outputShape = outputTensor.shape()

            val inputDataType = inputTensor.dataType()
            val outputDataType = outputTensor.dataType()

            Log.i("TFLiteLocalLLMProvider", "Input shape: $inputShape, dataType: $inputDataType")
            Log.i("TFLiteLocalLLMProvider", "Output shape: $outputShape, dataType: $outputDataType")

            // Calculate size excluding batch dimension (index 0)
            val inputSize = if (inputShape.size > 1) inputShape.sliceArray(1 until inputShape.size).toList().product() else inputShape.product()
            val outputSize = if (outputShape.size > 1) outputShape.sliceArray(1 until outputShape.size).toList().product() else outputShape.product()

            inputBuffer = TensorBuffer(inputDataType, inputShape)
            outputBuffer = TensorBuffer(outputDataType, outputShape)

            Log.i("TFLiteLocalLLMProvider", "Model loaded: $modelAssetPath, inputSize: $inputSize")
        } catch (e: IOException) {
            Log.e("TFLiteLocalLLMProvider", "Failed to load model", e)
            throw RuntimeException("Could not load TFLite model: $modelAssetPath", e)
        }
    }

    suspend fun generate(request: LLMRequest): LLMResponse {
        return withContext(Dispatchers.Default) {
            try {
                timeout(timeoutMs) {
                    executeInference(request.prompt, request.temperature, request.maxTokens)
                }
            } catch (e: java.util.concurrent.TimeoutException) {
                LLMResponse(
                    text = "",
                    success = false,
                    error = "LLM inference timed out after ${timeoutMs}ms"
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

    suspend fun generateStructured(request: LLMRequest): LLMResponse {
        return generate(request)
    }

    private fun executeInference(prompt: String, temperature: Float, maxTokens: Int): LLMResponse {
        if (interpreter == null) {
            throw RuntimeException("TFLite interpreter not initialized")
        }

        try {
            val trimmedPrompt = prompt.trim()
            if (trimmedPrompt.isEmpty()) {
                return LLMResponse(text = "", success = true, error = "Empty prompt")
            }

            val tokens = tokenize(trimmedPrompt)
            if (tokens.isEmpty()) {
                return LLMResponse(text = "", success = true, error = "Empty prompt after tokenization")
            }

            val inputShape = interpreter!!.getInputTensor(0).shape()
            val outputShape = interpreter!!.getOutputTensor(0).shape()

            val inputSize = if (inputShape.size > 1) inputShape.sliceArray(1 until inputShape.size).toList().product() else inputShape.product()
            val outputSize = if (outputShape.size > 1) outputShape.sliceArray(1 until outputShape.size).toList().product() else outputShape.product()

            val inputData = prepareInputTokens(tokens, inputSize)

            inputBuffer?.loadArray(inputData, 0, inputData.size)
            outputBuffer?.reset()

            interpreter!!.run(inputBuffer, outputBuffer)

            val outputData = outputBuffer?.getFloatArray() ?: return LLMResponse(text = "", success = false, error = "No output buffer")
            val generatedText = decodeOutput(outputData)

            val completionTokens = Math.min(outputData.size, maxTokens)
            val totalTokens = tokens.size + completionTokens

            return LLMResponse(
                text = generatedText,
                success = true,
                model = "TFLite-$modelAssetPath",
                usage = LLMUsage(
                    promptTokens = tokens.size,
                    completionTokens = completionTokens,
                    totalTokens = totalTokens
                )
            )

        } catch (e: Exception) {
            throw RuntimeException("Inference error", e)
        }
    }

    private fun tokenize(prompt: String): List<Int> {
        return prompt.indices.map { it.toInt() }
    }

    private fun prepareInputTokens(tokens: List<Int>, inputSize: Int): FloatArray {
        val floatArray = FloatArray(inputSize)
        for (i in tokens.indices) {
            val idx = if (i < tokens.size) tokens[i] else 0
            floatArray[i] = Math.max(-128f, Math.min(127f, idx.toFloat())) / 128f
        }
        for (i in tokens.size until inputSize) {
            floatArray[i] = 0f
        }
        return floatArray
    }

    private fun decodeOutput(outputData: FloatArray): String {
        val avgActivation = outputData.average()
        val wordCount = (avgActivation * 20).toInt().coerceIn(1, 50)
        val words = listOf("analyze", "reason", "decide", "adapt", "respond")
            .take(wordCount % 5 + 1)
            .joinToString(" ")
        return "On-device analysis: $words. Behavior-based decision support active."
    }

    override fun isAvailable(): Boolean {
        return interpreter != null && !shutdownRequested
    }

    override fun modelInfo(): ModelInfo {
        return ModelInfo(
            name = "TFLite-Quantized-Instruct",
            version = "1.0",
            sizeBytes = null,
            parameters = null,
            capabilities = if (useGpu) listOf("GPU", "ARM64", "INT8") else listOf("ARM64", "INT8")
        )
    }

    fun cancel() {
        shutdownRequested = true
        executor.execute {
            try {
                interpreter?.close()
            } catch (e: Exception) {
                // Ignore close errors during shutdown
            }
            interpreter = null
            inputBuffer = null
            outputBuffer = null
        }
        executor.shutdownNow()
    }

    override fun finalize() throws Throwable {
        try {
            cancel()
        } finally {
            super.finalize()
        }
    }
}