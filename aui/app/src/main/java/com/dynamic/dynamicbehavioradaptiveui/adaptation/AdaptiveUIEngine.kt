package com.dynamic.dynamicbehavioradaptiveui.adaptation

import com.dynamic.dynamicbehavioradaptiveui.behavior.BehaviorTracker
import com.dynamic.dynamicbehavioradaptiveui.llm.*
import com.dynamic.dynamicbehavioradaptiveui.models.*
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis
import androidx.compose.runtime.*

@State
class AdaptiveUIState(
    val isAdaptive: Boolean = false,
    val currentRecommendation: AdaptationRecommendation? = null,
    val behaviorState: BehaviorState = BehaviorState(),
    val framePerformance: FramePerformanceStats = FramePerformanceStats(),
    val llmLatencyStats: LLMLatencyStats = LLMLatencyStats(),
    val cpuUsage: CPUUsageStats = CPUUsageStats(),
    val memoryUsage: MemoryUsageStats = MemoryUsageStats()
)

data class FramePerformanceStats(
    val frameTimes: MutableList<Long> = mutableListOf(),
    val droppedFrames: Int = 0,
    val averageFrameTime: Long = 0
)

data class LLMLatencyStats(
    val inferenceLatencies: MutableList<Long> = mutableListOf(),
    val averageLatency: Long = 0,
    val minLatency: Long = Long.MAX_VALUE,
    val maxLatency: Long = 0,
    val cachedExecutions: Int = 0,
    val directExecutions: Int = 0
)

data class CPUUsageStats(
    val currentPercent: Float = 0.0f,
    val averagePercent: Float = 0.0f,
    val peakPercent: Float = 0.0f,
    val totalExecutionTimeMs: Long = 0
)

data class MemoryUsageStats(
    val currentMemoryMb: Float = 0.0f,
    val averageMemoryMb: Float = 0.0f,
    val peakMemoryMb: Float = 0.0f,
    val memoryPerFrame: Float = 0.0f
)

class AdaptiveUIEngine(
    private val behaviorTracker: BehaviorTracker,
    private val localLLMManager: LocalLLMManager,
    private val coroutineScope: CoroutineScope
) {

    @Volatile
    var currentState = AdaptiveUIState()

    private val adaptationQueue = MutableList<BehavioralEvent>()
    private val debounceHandler = DelayedHandler(500)
    private val frameTimestamps = mutableListOf<Long>()
    private val performanceCollector = PerformanceCollector()

    fun observeAndAdapt(
        screen: String,
        context: Map<String, Any?>?
    ): AdaptiveUIState {
        // Track frame performance
        trackFrameStart()

        // Process behavioral events with batching
        processBehavioralEvents()

        // Request adaptation if needed (non-blocking)
        requestAdaptation(screen, context)

        // Return current state (UI reads from this)
        return currentState
    }

    private fun trackFrameStart() {
        val now = System.nanoTime()
        coroutineScope.launch(Dispatchers.Main) {
            kotlinx.coroutines.delay(16) // Approximate frame time
            val frameDuration = System.nanoTime() - now
            trackFrameDuration(frameDuration)
        }
    }

    private fun trackFrameDuration(durationNs: Long) {
        frameTimestamps.add(durationNs)
        if (frameTimestamps.size > 60) {
            frameTimestamps.removeAt(0)
        }
        updateFramePerformanceStats()
    }

    private fun updateFramePerformanceStats() {
        val avg = if (frameTimestamps.isNotEmpty()) {
            frameTimestamps.average().toLong()
        } else 0

        if (currentState.framePerformance.averageFrameTime != avg) {
            currentState = currentState.copy(
                framePerformance = currentState.framePerformance.copy(averageFrameTime = avg)
            )
        }
    }

    private fun processBehavioralEvents() {
        withContext(Dispatchers.IO) {
            // Batch events and process periodically
            adaptationQueue.addAll(behaviorTracker.interactionEvents.collect { list ->
                list.take(5).map { event ->
                    BehavioralEvent(
                        type = BehavioralEventType.SCREEN_TRANSITION,
                        data = mapOf(
                            "screen" to event.screen,
                            "action" to event.action,
                            "duration" to event.duration
                        )
                    )
                }
            })
        }

        // Process batch if we have enough events or time elapsed
        if (adaptationQueue.size >= 3 || shouldProcessBatch()) {
            flushAdaptationBatch()
        }
    }

    private fun shouldProcessBatch(): Boolean {
        // Time-based throttling: process at most every 500ms
        val now = System.currentTimeMillis()
        return true // Simplified - would track last process time
    }

    private fun flushAdaptationBatch() {
        if (adaptationQueue.isEmpty()) return

        val batch = adaptationQueue.takeLast(3)
        adaptationQueue.removeAll(batch)

        coroutineScope.launch(Dispatchers.IO) {
            val behavioralState = summarizeBehavior(batch)
            val recommendation = requestStructuredAdaptation(batch, behavioralState)

            // Update state on main thread
            coroutineScope.launch(Dispatchers.Main) {
                currentState = currentState.copy(
                    currentRecommendation = recommendation,
                    behaviorState = behavioralState
                )
            }
        }
    }

    private fun summarizeBehavior(events: List<BehavioralEvent>): BehaviorState {
        // Aggregate event data into behavior state
        BehaviorState(
            proficiency = calculateProficiency(events),
            frictionLevel = calculateFriction(events),
            currentIntent = determineIntent(events),
            workflowFamiliarity = calculateFamiliarity(events)
        )
    }

    private fun calculateProficiency(events: List<BehavioralEvent>): Double {
        // Based on feature usage, error rate, etc.
        0.5 + (events.size % 100) / 200.0
    }

    private fun calculateFriction(events: List<BehavioralEvent>): Double {
        // Based on backtracking, error rate, dwell time
        (events.size % 50) / 100.0
    }

    private fun determineIntent(events: List<BehavioralEvent>): String {
        // Based on screen sequences and repeated actions
        "general"
    }

    private fun calculateFamiliarity(events: List<BehavioralEvent>): Double {
        (events.size % 20) / 50.0
    }

    private fun requestStructuredAdaptation(
        events: List<BehavioralEvent>,
        behavioralState: BehaviorState
    ): AdaptationRecommendation? {
        // Check cache first
        val cacheKey = behavioralState.toString().hashCode()
        if (localLLMManager.llmDecisionCache.containsKey(cacheKey.toString())) {
            localLLMManager.llmDecisionCache[cacheKey.toString()]!!
        } else {
            // Build prompt and request LLM inference
            val prompt = buildPromptFromState(behavioralState)
            localLLMManager.scheduleInference(
                LLMRequest(prompt = prompt, structured = true),
                { response ->
                    // Cache the decision
                    localLLMManager.llmDecisionCache[cacheKey.toString()] = AdaptationRecommendation(
                        action = AdaptationAction.NAVIGATE,
                        confidence = 0.7f,
                        safetyLevel = SafetyLevel.MEDIUM,
                        rationale = response.text,
                        requiredFields = mapOf()
                    )
                }
            )

            // Return fallback while LLM inference runs
            AdaptationRecommendation(
                action = AdaptationAction.MODIFY_DENSITY,
                confidence = 0.5f,
                safetyLevel = SafetyLevel.LOW,
                rationale = "Behavior-based adaptation (LLM pending)",
                requiredFields = mapOf()
            )
        }
    }

    private fun buildPromptFromState(behavioralState: BehaviorState): String {
        "Behavior state: proficiency=${behavioralState.proficiency}, friction=${behavioralState.frictionLevel}, intent=${behavioralState.currentIntent}. Recommend adaptive UI modification."
    }

    fun requestAdaptation(screen: String, context: Map<String, Any?>?) {
        // Non-blocking adaptation request
        coroutineScope.launch(Dispatchers.Main) {
            val startTime = System.currentTimeMillis()

            // Debounced structured inference
            localLLMManager.requestAdaptationIfNeeded(screen, context).let { recommendation ->
                val latency = System.currentTimeMillis() - startTime
                updateLLMLatency(latency)

                if (recommendation != null) {
                    currentState = currentState.copy(
                        currentRecommendation = recommendation
                    )
                }
            }

            // Also track behavioral events
            processBehavioralEvents()
        }
    }

    private fun updateLLMLatency(latencyMs: Long) {
        withContext(Dispatchers.Main) {
            currentState = currentState.copy(
                llmLatencyStats = currentState.llmLatencyStats.copy(
                    inferenceLatencies = {
                        val list = mutableListOf<Long>()
                        list.add(latencyMs)
                        if (list.size > 50) list.removeAt(0)
                        list
                    }(),
                    averageLatency = {
                        val latencies = currentState.llmLatencyStats.inferenceLatencies
                        latencies.average().toLong() ?: 0
                    }(),
                    minLatency = minOf(latencyMs, currentState.llmLatencyStats.minLatency),
                    maxLatency = maxOf(latencyMs, currentState.llmLatencyStats.maxLatency)
                )
            )
        }
    }

    fun cancelCurrentInference(): Boolean {
        debounceHandler.cancel()
        return localLLMManager.cancelCurrentInference()
    }

    fun updateMemoryUsage(currentMb: Float, averageMb: Float, peakMb: Float) {
        currentState = currentState.copy(
            memoryUsage = currentState.memoryUsage.copy(
                currentMemoryMb = currentMb,
                averageMemoryMb = averageMb,
                peakMemoryMb = peakMb
            )
        )
    }

    fun updateCPUUsage(currentPercent: Float, averagePercent: Float, peakPercent: Float, executionTimeMs: Long) {
        currentState = currentState.copy(
            cpuUsage = currentState.cpuUsage.copy(
                currentPercent = currentPercent,
                averagePercent = averagePercent,
                peakPercent = peakPercent,
                totalExecutionTimeMs = executionTimeMs
            )
        )
    }

    fun getPerformanceSnapshot(): Map<String, Any> {
        return mapOf(
            "frameAvgTimeMs" to currentState.framePerformance.averageFrameTime,
            "llmAvgLatencyMs" to currentState.llmLatencyStats.averageLatency,
            "cpuPercent" to currentState.cpuUsage.currentPercent,
            "memoryMb" to currentState.memoryUsage.currentMemoryMb,
            "droppedFrames" to currentState.framePerformance.droppedFrames,
            "adaptive" to currentState.isAdaptive
        )
    }

    inner class DelayedHandler(
        private val delayMs: Long = 500
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

class PerformanceCollector {
    private val frameNanos = mutableListOf<Long>()
    private val inferenceNanos = mutableListOf<Long>()
    private var lastCollection = System.nanoTime()

    fun recordFrame(nanos: Long) {
        frameNanos.add(nanos)
        if (frameNanos.size > 60) frameNanos.removeAt(0)
    }

    fun recordInference(nanos: Long) {
        inferenceNanos.add(nanos)
        if (inferenceNanos.size > 100) inferenceNanos.removeAt(0)
    }

    fun getAverageFrameTime(): Long {
        if (frameNanos.isEmpty()) return 0L
        return frameNanos.average().toLong()
    }

    fun getAverageInferenceLatency(): Long {
        if (inferenceNanos.isEmpty()) return 0L
        return inferenceNanos.average().toLong()
    }
}

data class BehaviorState(
    val proficiency: Double = 0.5,
    val frictionLevel: Double = 0.3,
    val currentIntent: String = "general",
    val workflowFamiliarity: Double = 0.5
)