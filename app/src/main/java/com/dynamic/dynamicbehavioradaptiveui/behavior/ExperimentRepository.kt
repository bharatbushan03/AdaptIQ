package com.dynamic.dynamicbehavioradaptiveui.behavior

import com.dynamic.dynamicbehavioradaptiveui.models.*
import androidx.room.Room
import android.app.Application
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.time.Instant
import java.util.UUID

class ExperimentRepository(
    private val db: InteractionEventDatabase = RoomDatabaseHolder.getDatabase(null as Application)
) : BehaviorTracker {

    private var currentMode: ExperimentMode = ExperimentMode.BASELINE
    private var currentSessionId: String = ""
    private var sessionStartTime: Long = 0L
    private var currentTaskId: String = ""
    private var taskStartTime: Long = 0L
    private var taskInteractionCount: Int = 0
    private var taskBacktrackCount: Int = 0
    private var taskErrorCount: Int = 0
    private var taskCompletionTime: Long = 0L
    private var taskAbandoned: Boolean = false
    private var featureDiscoverabilitySeen: Int = 0
    private var totalFeatures: Int = 0

    init {
        startNewSession()
    }

    fun startNewSession() {
        currentMode = if (java.util.random.nextBoolean()) ExperimentMode.BASELINE else ExperimentMode.ADAPTIVE
        currentSessionId = UUID.randomUUID().toString()
        sessionStartTime = Instant.now().toEpochMilli()
        currentTaskId = UUID.randomUUID().toString()
        taskStartTime = Instant.now().toEpochMilli()
        taskInteractionCount = 0
        taskBacktrackCount = 0
        taskErrorCount = 0
        taskCompletionTime = 0L
        taskAbandoned = false
        featureDiscoverabilitySeen = 0
        totalFeatures = 0

        db.experimentSessionDao().insert(ExperimentSession(
            sessionId = currentSessionId,
            mode = currentMode,
            startTime = sessionStartTime,
            endTime = 0L,
            baselineTaskCount = 0,
            adaptiveTaskCount = 0,
            status = ExperimentStatus.ACTIVE,
            notes = ""
        ))
    }

    fun switchMode() {
        currentMode = if (currentMode == ExperimentMode.BASELINE) ExperimentMode.ADAPTIVE else ExperimentMode.BASELINE
        sessionStartTime = Instant.now().toEpochMilli()
    }

    fun getCurrentMode(): ExperimentMode = currentMode

    suspend fun trackScreenOpened(screen: String, previousScreen: String?, duration: Long?, metadata: Map<String, String>?) {
        withContext(Dispatchers.IO) {
            val event = RoomInteractionEvent(
                eventId = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                sessionId = currentSessionId,
                screen = screen,
                action = when (currentMode) {
                    ExperimentMode.BASELINE -> "baseline_screen_open"
                    else -> "adaptive_screen_open"
                },
                target = screen,
                previousScreen = previousScreen ?: "",
                duration = duration ?: 0L,
                workflowId = UUID.randomUUID().toString(),
                success = true,
                metadata = metadata?.toString() ?: ""
            )
            db.interactionEventDao().insert(event)
        }
        taskInteractionCount++
    }

    suspend fun trackButtonClicked(screen: String, target: String, action: String, metadata: Map<String, String>?) {
        withContext(Dispatchers.IO) {
            val event = RoomInteractionEvent(
                eventId = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                sessionId = currentSessionId,
                screen = screen,
                action = when (currentMode) {
                    ExperimentMode.BASELINE -> "baseline_button_click"
                    else -> "adaptive_button_click"
                },
                target = target,
                previousScreen = "",
                duration = 0L,
                workflowId = UUID.randomUUID().toString(),
                success = true,
                metadata = metadata?.toString() ?: ""
            )
            db.interactionEventDao().insert(event)
        }
        taskInteractionCount++
    }

    suspend fun trackFeatureSelected(screen: String, feature: String, target: String, metadata: Map<String, String>?) {
        withContext(Dispatchers.IO) {
            val event = RoomInteractionEvent(
                eventId = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                sessionId = currentSessionId,
                screen = screen,
                action = when (currentMode) {
                    ExperimentMode.BASELINE -> "baseline_feature_select"
                    else -> "adaptive_feature_select"
                },
                target = target,
                previousScreen = "",
                duration = 0L,
                workflowId = UUID.randomUUID().toString(),
                success = true,
                metadata = mapOf("feature" to feature, "features" to metadata?.toString() ?: "").toString()
            )
            db.interactionEventDao().insert(event)
        }
        taskInteractionCount++
        totalFeatures++
        featureDiscoverabilitySeen++
    }

    suspend fun trackNavigation(screen: String, targetScreen: String, action: String, metadata: Map<String, String>?) {
        withContext(Dispatchers.IO) {
            val event = RoomInteractionEvent(
                eventId = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                sessionId = currentSessionId,
                screen = screen,
                action = when (currentMode) {
                    ExperimentMode.BASELINE -> "baseline_navigation"
                    else -> "adaptive_navigation"
                },
                target = targetScreen,
                previousScreen = "",
                duration = 0L,
                workflowId = UUID.randomUUID().toString(),
                success = true,
                metadata = metadata?.toString() ?: ""
            )
            db.interactionEventDao().insert(event)
        }
        taskInteractionCount++
    }

    suspend fun trackBackNavigation(screen: String, previousScreen: String, metadata: Map<String, String>?) {
        withContext(Dispatchers.IO) {
            val event = RoomInteractionEvent(
                eventId = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                sessionId = currentSessionId,
                screen = screen,
                action = when (currentMode) {
                    ExperimentMode.BASELINE -> "baseline_back_navigation"
                    else -> "adaptive_back_navigation"
                },
                target = previousScreen,
                previousScreen = previousScreen,
                duration = 0L,
                workflowId = UUID.randomUUID().toString(),
                success = true,
                metadata = metadata?.toString() ?: ""
            )
            db.interactionEventDao().insert(event)
        }
        taskInteractionCount++
        taskBacktrackCount++
    }

    suspend fun trackDwellTime(screen: String, duration: Long, metadata: Map<String, String>?) {
        withContext(Dispatchers.IO) {
            val event = RoomInteractionEvent(
                eventId = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                sessionId = currentSessionId,
                screen = screen,
                action = when (currentMode) {
                    ExperimentMode.BASELINE -> "baseline_dwell_time"
                    else -> "adaptive_dwell_time"
                },
                target = screen,
                previousScreen = "",
                duration = duration,
                workflowId = UUID.randomUUID().toString(),
                success = true,
                metadata = metadata?.toString() ?: ""
            )
            db.interactionEventDao().insert(event)
        }
    }

    suspend fun trackInteractionError(screen: String, action: String, error: String, metadata: Map<String, String>?) {
        withContext(Dispatchers.IO) {
            val event = RoomInteractionEvent(
                eventId = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                sessionId = currentSessionId,
                screen = screen,
                action = when (currentMode) {
                    ExperimentMode.BASELINE -> "baseline_interaction_error"
                    else -> "adaptive_interaction_error"
                },
                target = error,
                previousScreen = "",
                duration = 0L,
                workflowId = UUID.randomUUID().toString(),
                success = false,
                metadata = mapOf("features" to error, "features" to metadata?.toString() ?: "").toString()
            )
            db.interactionEventDao().insert(event)
        }
        taskErrorCount++
    }

    suspend fun trackTaskCompletionTime(taskId: String, duration: Long, success: Boolean, metadata: Map<String, String>?) {
        withContext(Dispatchers.IO) {
            val event = RoomInteractionEvent(
                eventId = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                sessionId = currentSessionId,
                screen = "",
                action = when (currentMode) {
                    ExperimentMode.BASELINE -> "baseline_task_completion_time"
                    else -> "adaptive_task_completion_time"
                },
                target = taskId,
                previousScreen = "",
                duration = duration,
                workflowId = UUID.randomUUID().toString(),
                success = success ? 1 : 0,
                metadata = mapOf("features" to metadata?.toString() ?: "").toString()
            )
            db.interactionEventDao().insert(event)
        }
        if (success) {
            taskCompletionTime += duration
        }
    }

    suspend fun trackFeatureUsageFrequency(feature: String, count: Int, metadata: Map<String, String>?) {
        withContext(Dispatchers.IO) {
            val event = RoomInteractionEvent(
                eventId = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                sessionId = currentSessionId,
                screen = "",
                action = when (currentMode) {
                    ExperimentMode.BASELINE -> "baseline_feature_usage"
                    else -> "adaptive_feature_usage"
                },
                target = feature,
                previousScreen = "",
                duration = 0L,
                workflowId = UUID.randomUUID().toString(),
                success = true,
                metadata = mapOf("features" to count.toString(), "features" to metadata?.toString() ?: "").toString()
            )
            db.interactionEventDao().insert(event)
        }
    }

    fun completeCurrentTask(taskId: String?, success: Boolean, metadata: Map<String, String>?) {
        val endTime = Instant.now().toEpochMilli()
        val duration = endTime - taskStartTime

        val completedTaskId = taskId ?: currentTaskId
        taskCompletionTime += duration

        withContext(Dispatchers.IO) {
            val metrics = ExperimentMetrics(
                metricsId = UUID.randomUUID().toString(),
                sessionId = currentSessionId,
                taskId = completedTaskId,
                mode = currentMode,
                taskCompletionTimeMs = duration,
                numInteractions = taskInteractionCount,
                navigationDepth = taskInteractionCount,
                backtrackingCount = taskBacktrackCount,
                errorCount = taskErrorCount,
                abandonment = taskAbandoned,
                featureDiscoverability = if (totalFeatures > 0) featureDiscoverabilitySeen.toDouble() / totalFeatures else 0.0,
                timestamp = endTime
            )
            db.experimentMetricsDao().insertMetrics(metrics)

            db.experimentSessionDao().let { dao ->
                when (currentMode) {
                    ExperimentMode.BASELINE -> {
                        dao.insertSession(ExperimentSession(
                            sessionId = currentSessionId,
                            mode = ExperimentMode.BASELINE,
                            startTime = sessionStartTime,
                            endTime = endTime,
                            baselineTaskCount = 1,
                            adaptiveTaskCount = 0,
                            status = ExperimentStatus.COMPLETED,
                            notes = ""
                        ))
                    }
                    ExperimentMode.ADAPTIVE -> {
                        dao.insertSession(ExperimentSession(
                            sessionId = currentSessionId,
                            mode = ExperimentMode.ADAPTIVE,
                            startTime = sessionStartTime,
                            endTime = endTime,
                            baselineTaskCount = 0,
                            adaptiveTaskCount = 1,
                            status = ExperimentStatus.COMPLETED,
                            notes = ""
                        ))
                    }
                }
            }
        }
    }

    fun abandonCurrentTask() {
        taskAbandoned = true
        completeCurrentTask(null, false, null)
    }

    fun markFeatureDiscovered() {
        // Called when a feature is discovered during exploration
        // This is tracked in trackFeatureSelected already via totalFeatures/count
    }

    fun getSessionStats(): Pair<Double, Double> {
        val sessions = db.experimentSessionDao().getAllSessions().firstOrNull()?.firstOrNull() ?: return Pair(0.0, 0.0)
        val metrics = db.experimentMetricsDao().getMetricsByMode(ExperimentMode.BASELINE).firstOrNull() ?: emptyList()
        val adaptiveMetrics = db.experimentMetricsDao().getMetricsByMode(ExperimentMode.ADAPTIVE).firstOrNull() ?: emptyList()

        val baselineAvgTime = if (metrics.isNotEmpty()) metrics.averageBy { it.taskCompletionTimeMs } / 1000.0 else 0.0
        val adaptiveAvgTime = if (adaptiveMetrics.isNotEmpty()) adaptiveMetrics.averageBy { it.taskCompletionTimeMs } / 1000.0 else 0.0

        val baselineAvgInteractions = if (metrics.isNotEmpty()) metrics.averageBy { it.numInteractions }.toDouble() else 0.0
        val adaptiveAvgInteractions = if (adaptiveMetrics.isNotEmpty()) adaptiveMetrics.averageBy { it.numInteractions }.toDouble() else 0.0

        return Pair(baselineAvgTime, adaptiveAvgTime).also { improvements ->
            improvements.second.also { adaptive ->
                if (baselineAvgTime > 0) {
                    improvements.first = (1.0 - adaptive / baselineAvgTime) * 100.0
                }
            }
        }
    }
}