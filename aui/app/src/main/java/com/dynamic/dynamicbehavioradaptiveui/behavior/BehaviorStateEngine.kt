package com.dynamic.dynamicbehavioradaptiveui.behavior

import com.dynamic.dynamicbehavioradaptiveui.models.BehaviorState
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

class BehaviorStateEngine(
    private val coroutineScope: CoroutineScope,
    private val telemetry: BehaviorTracker
) {

    @Volatile
    private var currentState = BehaviorState(
        proficiency = 0.5,
        frictionLevel = 0.3,
        currentIntent = "general",
        workflowFamiliarity = 0.5
    )

    private val stateLock = Any()
    private val inferenceCache = ConcurrentHashMap<String, BehaviorState>()
    private val debounceHandler = DelayedHandler(500)
    private val behavioralEventBatch = MutableList<BehavioralEvent>()
    private var adaptiveFrequency = 1000L
    private var lastStateUpdate = 0L

    fun getCurrentState(): BehaviorState = currentState

    fun updateStateFromEvent(event: BehavioralEvent) {
        debounceHandler.reset {
            coroutineScope.launch(Dispatchers.IO) {
                synchronized(stateLock) {
                    // Accumulate statistics from event
                    updateStateInternal(event)
                    lastStateUpdate = System.currentTimeMillis()

                    // Cache the updated state
                    val cacheKey = "${event.type}_${System.currentTimeMillis()}"
                    inferenceCache[cacheKey] = currentState

                    // Adaptive frequency throttling
                    val now = System.currentTimeMillis()
                    if (now - lastStateUpdate < adaptiveFrequency) {
                        return@launch
                    }

                    // Emit updated state
                    // State is available via getCurrentState() for UI composition
                }
            }
        }
    }

    private fun updateStateInternal(event: BehavioralEvent) {
        // Proficiency: increases with successful task completion, decreases with errors
        when {
            event.data["action"] == "task_complete" && event.data["success"] == true -> {
                currentState = currentState.copy(
                    proficiency = minOf(currentState.proficiency + 0.05, 1.0)
                )
            }
            event.data["action"] == "interaction_error" -> {
                currentState = currentState.copy(
                    proficiency = maxOf(currentState.proficiency - 0.02, 0.0)
                )
            }
            else -> {}
        }

        // Friction level: based on backtracking frequency and error rate
        currentState = currentState.copy(
            frictionLevel = if (event.data.containsKey("backtrack")) {
                minOf(currentState.frictionLevel + 0.1, 1.0)
            } else {
                maxOf(currentState.frictionLevel - 0.05, 0.0)
            }
        )

        // Current intent: based on screen sequences and repeated actions
        when {
            event.data["screen"] == "tasks" && event.data["action"] == "add_task" -> {
                currentState = currentState.copy(currentIntent = "task_creation")
            }
            event.data["screen"] == "calendar" && event.data["action"] == "add_event" -> {
                currentState = currentState.copy(currentIntent = "event_scheduling")
            }
            else -> {}
        }

        // Workflow familiarity: based on feature/action repeats
        currentState = currentState.copy(
            workflowFamiliarity = if (behavioralEventBatch.size > 5) {
                minOf(currentState.workflowFamiliarity + 0.02, 1.0)
            } else {
                maxOf(currentState.workflowFamiliarity - 0.02, 0.0)
            }
        )

        // Batch events for periodic analysis
        behavioralEventBatch.add(event)
        if (behavioralEventBatch.size >= 5) {
            flushBatchAnalysis()
        }
    }

    private fun flushBatchAnalysis() {
        if (behavioralEventBatch.isEmpty()) return

        val batch = behavioralEventBatch.takeLast(5)
        behavioralEventBatch.removeAll(batch)

        coroutineScope.launch(Dispatchers.IO) {
            // Analyze batch and update state
            val proficiencyScore = batch.average { 
                when (it.data["action"] as? String) {
                    "task_complete" -> 1.0
                    "interaction_error" -> 0.0
                    else -> 0.5
                }
            }

            val frictionScore = batch.average { 
                when (it.data["backtrack"] as? Boolean) {
                    true -> 1.0
                    false -> 0.2
                }
            }

            withContext(Dispatchers.Main) {
                currentState = currentState.copy(
                    proficiency = proficiencyScore,
                    frictionLevel = frictionScore
                )
            }
        }
    }

    fun cancelCurrentAnalysis(): Boolean {
        debounceHandler.cancel()
        return true
    }

fun setAdaptiveFrequency(frequencyMs: Long) {
    this.adaptiveFrequency = frequencyMs
}

    companion object {
        private val DefaultAdaptiveFrequency = 1000L
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