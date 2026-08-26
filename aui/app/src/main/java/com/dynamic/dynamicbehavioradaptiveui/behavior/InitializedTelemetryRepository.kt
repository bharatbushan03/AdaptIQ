package com.dynamic.dynamicbehavioradaptiveui.behavior

import com.dynamic.dynamicbehavioradaptiveui.models.RoomDatabaseHolder
import kotlinx.coroutines.BuilderCoroutineScope
import kotlinx.coroutines.Dispatchers

class InitializedTelemetryRepository(
    private val interactionEventDao: InteractionEventDao,
    private val experimentSessionDao: ExperimentSessionDao,
    private val experimentMetricsDao: ExperimentMetricsDao
) : BehaviorTracker {

    private val coroutineScope = BuilderCoroutineScope()

    override suspend fun trackScreenOpened(
        screen: String,
        sessionId: String?,
        previousScreen: String?,
        workflowId: String?
    ) {
        withContext(Dispatchers.IO) {
            val event = InteractionEvent(
                eventId = UUID.randomUUID().toString(),
                timestamp = java.time.Instant.now().toString(),
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
            interactionEventDao.insert(event)
        }
    }

    override suspend fun trackButtonClicked(
        screen: String,
        buttonId: String,
        action: String,
        metadata: String?
    ) {
        withContext(Dispatchers.IO) {
            val event = InteractionEvent(
                eventId = UUID.randomUUID().toString(),
                timestamp = java.time.Instant.now().toString(),
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
            interactionEventDao.insert(event)
        }
    }

    override suspend fun trackFeatureUsageFrequency(
        feature: String,
        frequency: Int,
        metadata: String?
    ) {
        withContext(Dispatchers.IO) {
            val event = InteractionEvent(
                eventId = UUID.randomUUID().toString(),
                timestamp = java.time.Instant.now().toString(),
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
            interactionEventDao.insert(event)
        }
    }

    override suspend fun trackTaskCompletionTime(
        task: String,
        durationMs: Long,
        success: Boolean,
        metadata: String?
    ) {
        withContext(Dispatchers.IO) {
            val event = InteractionEvent(
                eventId = UUID.randomUUID().toString(),
                timestamp = java.time.Instant.now().toString(),
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
            interactionEventDao.insert(event)
        }
    }

    override suspend fun trackInteractionEvent(event: InteractionEvent) {
        withContext(Dispatchers.IO) {
            interactionEventDao.insert(event)
        }
    }

    override suspend fun trackInteractionError(
        screen: String,
        error: String,
        metadata: String?
    ) {
        withContext(Dispatchers.IO) {
            val event = InteractionEvent(
                eventId = UUID.randomUUID().toString(),
                timestamp = java.time.Instant.now().toString(),
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
            interactionEventDao.insert(event)
        }
    }

    override suspend fun trackBackNavigation(
        screen: String,
        targetScreen: String,
        metadata: String?
    ) {
        withContext(Dispatchers.IO) {
            val event = InteractionEvent(
                eventId = UUID.randomUUID().toString(),
                timestamp = java.time.Instant.now().toString(),
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
            interactionEventDao.insert(event)
        }
    }

    override suspend fun trackSessionStart(
        mode: String,
        sessionId: String
    ) {
        withContext(Dispatchers.IO) {
            val session = ExperimentSession(
                sessionId = sessionId,
                mode = mode,
                startTime = java.time.Instant.now().toString(),
                endTime = null,
                taskCount = 0L,
                interactionCount = 0L,
                backtrackCount = 0,
                errorCount = 0
            )
            experimentSessionDao.insert(session)
        }
    }

    override suspend fun trackSessionEnd(
        sessionId: String,
        durationMs: Long,
        metrics: Map<String, Double>
    ) {
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

    override val interactionEvents: Flow<List<InteractionEvent>>
        get() = TODO("Not implemented in simplified version")

    override val sessionStats: Flow<Pair<Double, Double>>
        get() = TODO("Not implemented in simplified version")
}

object InitializedTelemetryRepository {

    @Volatile
    private var instance: InitializedTelemetryRepository? = null

    fun getInstance(
        interactionEventDao: InteractionEventDao,
        experimentSessionDao: ExperimentSessionDao,
        experimentMetricsDao: ExperimentMetricsDao
    ): InitializedTelemetryRepository {
        return instance ?: synchronized(this) {
            val initialized = InitializedTelemetryRepository(
                interactionEventDao = interactionEventDao,
                experimentSessionDao = experimentSessionDao,
                experimentMetricsDao = experimentMetricsDao
            )
            instance = initialized
            initialized
        }
    }

    val CoroutineScope: kotlinx.coroutines.CoroutineScope
        get() = InitializedTelemetryRepository().coroutineScope
}