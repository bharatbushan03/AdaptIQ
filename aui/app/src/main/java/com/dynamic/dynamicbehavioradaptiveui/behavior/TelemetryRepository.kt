package com.dynamic.dynamicbehavioradaptiveui.behavior

import com.dynamic.dynamicbehavioradaptiveui.models.InteractionEvent
import com.dynamic.dynamicbehavioradaptiveui.models.RoomInteractionEvent
import com.dynamic.dynamicbehavioradaptiveui.models.ExperimentSession
import com.dynamic.dynamicbehavioradaptiveui.models.ExperimentMetrics
import com.dynamic.dynamicbehavioradaptiveui.models.Task
import com.dynamic.dynamicbehavioradaptiveui.models.UserBehaviorProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.coroutines.intrinsic
import java.time.Instant
import java.util.*

class TelemetryRepository(
    private val interactionEventDao: InteractionEventDao,
    private val experimentSessionDao: ExperimentSessionDao,
    private val experimentMetricsDao: ExperimentMetricsDao,
    private val coroutineScope: kotlinx.coroutines.CoroutineScope
) : BehaviorTracker {

    private val _interactionEvents = MutableFlow<List<InteractionEvent>>()
    val interactionEvents: Flow<List<InteractionEvent>> = _interactionEvents.asFlow()

    private val _sessionStats = MutableFlow<Pair<Double, Double>>()
    val sessionStats: Flow<Pair<Double, Double>> = _sessionStats.asFlow()

    override suspend fun trackScreenOpened(
        screen: String,
        sessionId: String?,
        previousScreen: String?,
        workflowId: String?
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            val event = InteractionEvent(
                eventId = UUID.randomUUID().toString(),
                timestamp = Instant.now().toString(),
                sessionId = sessionId,
                screen = screen,
                action = "screen_open",
                target = null,
                previousScreen = previousScreen,
                duration = 0L,
                workflowId = workflowId,
                success = true,
                metadata = null
            )
            withContext(Dispatchers.IO) {
                interactionEventDao.insert(event)
            }
        }
    }

    override suspend fun trackButtonClicked(
        screen: String,
        buttonId: String,
        action: String,
        metadata: String?
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            val event = InteractionEvent(
                eventId = UUID.randomUUID().toString(),
                timestamp = Instant.now().toString(),
                sessionId = null,
                screen = screen,
                action = "button_click",
                target = buttonId,
                previousScreen = null,
                duration = 0L,
                workflowId = null,
                success = true,
                metadata = metadata
            )
            withContext(Dispatchers.IO) {
                interactionEventDao.insert(event)
            }
        }
    }

    override suspend fun trackFeatureUsageFrequency(
        feature: String,
        frequency: Int,
        metadata: String?
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            val event = InteractionEvent(
                eventId = UUID.randomUUID().toString(),
                timestamp = Instant.now().toString(),
                sessionId = null,
                screen = feature,
                action = "feature_usage",
                target = null,
                previousScreen = null,
                duration = 0L,
                workflowId = null,
                success = true,
                metadata = "$frequency"
            )
            withContext(Dispatchers.IO) {
                interactionEventDao.insert(event)
            }
        }
    }

    override suspend fun trackTaskCompletionTime(
        task: String,
        durationMs: Long,
        success: Boolean,
        metadata: String?
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            val event = InteractionEvent(
                eventId = UUID.randomUUID().toString(),
                timestamp = Instant.now().toString(),
                sessionId = null,
                screen = task,
                action = "task_completion",
                target = null,
                previousScreen = null,
                duration = durationMs,
                workflowId = null,
                success = success,
                metadata = metadata
            )
            withContext(Dispatchers.IO) {
                interactionEventDao.insert(event)
            }
        }
    }

    override suspend fun trackInteractionEvent(event: InteractionEvent) {
        coroutineScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.IO) {
                interactionEventDao.insert(event)
            }
        }
    }

    override suspend fun trackInteractionError(
        screen: String,
        error: String,
        metadata: String?
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            val event = InteractionEvent(
                eventId = UUID.randomUUID().toString(),
                timestamp = Instant.now().toString(),
                sessionId = null,
                screen = screen,
                action = "interaction_error",
                target = error,
                previousScreen = null,
                duration = 0L,
                workflowId = null,
                success = false,
                metadata = metadata
            )
            withContext(Dispatchers.IO) {
                interactionEventDao.insert(event)
            }
        }
    }

    override suspend fun trackBackNavigation(
        screen: String,
        targetScreen: String,
        metadata: String?
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            val event = InteractionEvent(
                eventId = UUID.randomUUID().toString(),
                timestamp = Instant.now().toString(),
                sessionId = null,
                screen = screen,
                action = "back_navigation",
                target = targetScreen,
                previousScreen = null,
                duration = 0L,
                workflowId = null,
                success = true,
                metadata = metadata
            )
            withContext(Dispatchers.IO) {
                interactionEventDao.insert(event)
            }
        }
    }

    override suspend fun trackSessionStart(
        mode: String,
        sessionId: String
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            val session = ExperimentSession(
                sessionId = sessionId,
                mode = mode,
                startTime = Instant.now().toString(),
                endTime = null,
                taskCount = 0L,
                interactionCount = 0L,
                backtrackCount = 0,
                errorCount = 0
            )
            withContext(Dispatchers.IO) {
                experimentSessionDao.insert(session)
            }
        }
    }

    override suspend fun trackSessionEnd(
        sessionId: String,
        durationMs: Long,
        metrics: Map<String, Double>
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.IO) {
                experimentSessionDao.endSession(sessionId, durationMs)
            }
            withContext(Dispatchers.IO) {
                experimentMetricsDao.insertOrUpdate(ExperimentMetrics(
                    sessionId = sessionId,
                    taskCompletionTime = metrics["taskCompletionTime"] ?: 0.0,
                    totalInteractions = metrics["totalInteractions"] ?: 0.0,
                    backtrackingFrequency = metrics["backtrackingFrequency"] ?: 0.0,
                    errorRate = metrics["errorRate"] ?: 0.0,
                    featureDiscoverability = metrics["featureDiscoverability"] ?: 0.0,
                    navigationPathCount = metrics["navigationPathCount"] ?: 0.0,
                    errorCount = metrics["errorCount"] ?: 0,
                    featureUsageCounts = metrics.toString()
                ))
            }
            _sessionStats.emit(metrics["effectiveness"] ?: 0.0 to metrics["adaptationRate"] ?: 0.0)
        }
    }

    fun emitInteractionEvent(event: InteractionEvent) {
        coroutineScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.IO) {
                interactionEventDao.insert(event)
            }
            _interactionEvents.emit(listOf(event))
        }
    }

    fun startSessionTracking(mode: String, sessionId: String) {
        coroutineScope.launch(Dispatchers.Main) {
            trackSessionStart(mode, sessionId)
        }
    }

    fun endSessionTracking(sessionId: String, durationMs: Long, metrics: Map<String, Double>) {
        coroutineScope.launch(Dispatchers.Main) {
            trackSessionEnd(sessionId, durationMs, metrics)
        }
    }
}