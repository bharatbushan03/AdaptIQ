package com.dynamic.dynamicbehavioradaptiveui.llm

import com.dynamic.dynamicbehavioradaptiveui.behavior.BehaviorTracker
import com.dynamic.dynamicbehavioradaptiveui.models.AdaptationRecommendation
import com.dynamic.dynamicbehavioradaptiveui.models.SafetyLevel
import com.dynamic.dynamicbehavioradaptiveui.models.AdaptationAction
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

class LocalLLMManager(
    private val localLLM: LocalLLM,
    private val behaviorTracker: BehaviorTracker,
    private val coroutineScope: CoroutineScope
) {

    private val inferenceQueue = MutableList<LLMRequest>()
    private val inferenceSemaphore = Semaphore(2)
    private val debounceHandler = DelayedHandler(300)
    private val batchProcessor = MutableList<LLMRequest>()
    private var batchFlushCoroutine: Job? = null
    private val cachedBehaviorState = MutableMap<String, Any>()
    private val llmDecisionCache = ConcurrentHashMap<String, AdaptationRecommendation>()
    private var adaptiveFrequency = 2000L  // ms between inferences
    private var lastInferenceTime = 0L
    private val maxBatchSize = 3
    private val inferenceCooldown = 1000L

    initUnit

    fun processBehavioralEvent(event: BehavioralEvent) {
        // Cache behavioral state
        cachedBehaviorState[event.type] = event.data

        // Debounced inference scheduling
        debounceHandler.reset {
            scheduleInferenceIfNeeded()
        }

        // Add to batch
        batchProcessor.add(event)
        if (batchProcessor.size >= maxBatchSize) {
            flushBatch()
        }
    }

    private fun scheduleInferenceIfNeeded() {
        val now = System.currentTimeMillis()
        if (now - lastInferenceTime < adaptiveFrequency) {
            return  // Adaptive frequency throttling
        }
        lastInferenceTime = now
        coroutineScope.launch(LLM_INFERENCE_SCOPE) {
            processQueuedInference()
        }
    }

    private fun processQueuedInference() {
        if (inferenceSemaphore.availablePermits() < 1) return

        // Take up to batch size requests from queue
        val requestsToProcess = mutableListOf<LLMRequest>()
        synchronization({
            inferenceQueue.takeIf { it.isNotEmpty() }?.take(maxBatchSize)?.forEach { requestsToProcess.add(it) }
        })

        if (requestsToProcess.isEmpty()) return

        inferenceSemaphore.acquire()
        coroutineScope.launch { processInferenceBatch(requestsToProcess) }
    }

    private fun processInferenceBatch(requests: MutableList<LLMRequest>) {
        try {
            val results = requests.map { req ->
                withContext(Dispatchers.Default) {
                    localLLM.generate(req)
                }
            }
            // Emit results to adaptation engine
            results.forEach { response ->
                behaviorTracker.trackInteractionEvent(
                    InteractionEvent(
                        eventId = UUID.randomUUID().toString(),
                        timestamp = java.time.Instant.now().toString(),
                        sessionId = null,
                        screen = "llm_inference",
                        action = "llm_result",
                        target = null,
                        previousScreen = null,
                        duration = response.latencyMs,
                        workflowId = null,
                        success = true,
                        metadata = response.text
                    )
                )
            }
        } catch (e: Exception) {
            // Log error, don't crash
            e.printStackTrace()
        } finally {
            inferenceSemaphore.release()
            scheduleNextInference()
        }
    }

    private fun scheduleNextInference() {
        coroutineScope.launch(LLM_INFERENCE_SCOPE) {
            kotlinx.coroutines.delay(adaptiveFrequency)
            processQueuedInference()
        }
    }

    fun requestAdaptationIfNeeded(screen: String, context: Map<String, Any?>?): AdaptationRecommendation? {
        // Check cached decision first
        val cacheKey = "${screen}_${context?.toString().hashCode()}"
        if (llmDecisionCache.containsKey(cacheKey)) {
            return llmDecisionCache[cacheKey]
        }

        // Build prompt from behavioral state
        val behavioralState = summarizeBehavioralState()
        val prompt = buildAdaptationPrompt(screen, behavioralState, context)

        // Debounced structured inference
        debounceHandler.reset {
            val request = LLMRequest(prompt = prompt, structured = true)
            localLLM.generateStructured(request.prompt, "AdaptationRecommendation", request.temperature, request.maxTokens).let { recommendation ->
                llmDecisionCache[cacheKey] = recommendation
                return@debounceHandler recommendation
            } ?: run {
                // Fall back to behavior-state-based adaptation without LLM
                fallbackAdaptation(screen, behavioralState)
            }
        }

        // Wait for debounced result (non-blocking - returns null if not ready yet)
        return null
    }

    private fun summarizeBehavioralState(): Map<String, Any> {
        // Return cached behavioral state or default
        return cachedBehaviorState.toMutableMap().let { state ->
            state + ("lastEvent" to BehavioralEventType.values().last()?.name ?: "none")
        }
    }

    private fun buildAdaptationPrompt(screen: String, behavioralState: Map<String, Any?>, context: Map<String, Any?>?): String {
        val stateSummary = behavioralState.entries.joinToString(", ") { "$it.key: ${it.value}" }
        "Screen: $screen. Behavioral state: $stateSummary. Context: ${context?.toString() ?: "none"}. Recommend adaptive UI action."
    }

    private fun fallbackAdaptation(screen: String, behavioralState: Map<String, Any>): AdaptationRecommendation {
        return AdaptationRecommendation(
            action = AdaptationAction.MODIFY_DENSITY,
            confidence = 0.6f,
            safetyLevel = SafetyLevel.LOW,
            rationale = "Fallback adaptation based on behavioral state: $screen",
            requiredFields = mapOf()
        )
    }

    fun addInferenceRequest(request: LLMRequest) {
        coroutineScope.launch(LLM_INFERENCE_SCOPE) {
            synchronization({
                inferenceQueue.add(request)
            })
            scheduleInferenceIfNeeded()
        }
    }

    fun cancelCurrentInference(): Boolean {
        debounceHandler.cancel()
        synchronization {
            inferenceQueue.clear()
            batchProcessor.clear()
        }
        return true
    }

    fun setAdaptiveFrequency(frequencyMs: Long) {
        this.adaptiveFrequency = frequencyMs
    }

    private fun flushBatch() {
        if (batchProcessor.isEmpty()) return

        val batch = batchProcessor.take()
        batchProcessor.clear()

        coroutineScope.launch(LLM_INFERENCE_SCOPE) {
            val results = batch.map { event ->
                withContext(Dispatchers.Default) {
                    localLLM.generate(LLMRequest(prompt = event.prompt))
                }
            }

            // Process results non-blocking
            results.forEach { response ->
                // Emit to behavior tracker or adaptation engine
            }
        }
    }

    companion object {
        const val DEFAULT_ADAPTIVE_FREQUENCY = 2000L
        const val MAX_INFERENCE_QUEUE = 10
        const val CACHE_SIZE = 100
    }
}

enum class BehavioralEventType {
    SCREEN_TRANSITION,
    BUTTON_CLICK,
    FEATURE_USAGE,
    TASK_COMPLETION,
    NAVIGATION
}

data class BehavioralEvent(
    val type: BehavioralEventType,
    val data: Map<String, Any>,
    val timestamp: Long = System.currentTimeMillis()
)

object LocalLLMManager {
    private var instance: LocalLLMManager? = null

    fun getInstance(
        localLLM: LocalLLM,
        behaviorTracker: BehaviorTracker,
        coroutineScope: CoroutineScope
    ): LocalLLMManager {
        return instance ?: synchronized(this) {
            val manager = LocalLLMManager(localLLM, behaviorTracker, coroutineScope)
            instance = manager
            manager
        }
    }
}

val LLM_INFERENCE_SCOPE = CoroutineName("LLM-Inference")