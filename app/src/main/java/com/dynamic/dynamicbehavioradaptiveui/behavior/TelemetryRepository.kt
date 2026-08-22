package com.dynamic.dynamicbehavioradaptiveui.behavior

import com.dynamic.dynamicbehavioradaptiveui.models.InteractionEvent
import com.dynamic.dynamicbehavioradaptiveui.models.RoomInteractionEvent
import com.dynamic.dynamicbehavioradaptiveui.models.InteractionEventDatabase
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.util.*

class TelemetryRepository(
    private val db: InteractionEventDatabase
) : BehaviorTracker {

    private val CoroutineScope = kotlinx.coroutines.BuilderCoroutineScope()

    override suspend fun trackScreenOpened(screen: String, previousScreen: String?, duration: Long?, metadata: Map<String, String>?) {
        withContext(Dispatchers.IO) {
            val event = RoomInteractionEvent(
                eventId = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                sessionId = UUID.randomUUID().toString(),
                screen = screen,
                action = "screen_opened",
                target = screen,
                previousScreen = previousScreen ?: "",
                duration = duration ?: 0L,
                workflowId = UUID.randomUUID().toString(),
                success = true,
                metadata = mapOf("features" to metadata?.toString() ?: "").toString()
            )
            db.interactionEventDao().insert(event)
        }
    }

    override suspend fun trackButtonClicked(screen: String, target: String, action: String, metadata: Map<String, String>?) {
        withContext(Dispatchers.IO) {
            val event = RoomInteractionEvent(
                eventId = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                sessionId = UUID.randomUUID().toString(),
                screen = screen,
                action = action,
                target = target,
                previousScreen = "",
                duration = 0L,
                workflowId = UUID.randomUUID().toString(),
                success = true,
                metadata = mapOf("features" to metadata?.toString() ?: "").toString()
            )
            db.interactionEventDao().insert(event)
        }
    }

    override suspend fun trackFeatureSelected(screen: String, feature: String, target: String, metadata: Map<String, String>?) {
        withContext(Dispatchers.IO) {
            val event = RoomInteractionEvent(
                eventId = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                sessionId = UUID.randomUUID().toString(),
                screen = screen,
                action = "feature_selected",
                target = target,
                previousScreen = "",
                duration = 0L,
                workflowId = UUID.randomUUID().toString(),
                success = true,
                metadata = mapOf("feature" to feature, "features" to metadata?.toString() ?: "").toString()
            )
            db.interactionEventDao().insert(event)
        }
    }

    override suspend fun trackNavigation(screen: String, targetScreen: String, action: String, metadata: Map<String, String>?) {
        withContext(Dispatchers.IO) {
            val event = RoomInteractionEvent(
                eventId = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                sessionId = UUID.randomUUID().toString(),
                screen = screen,
                action = action,
                target = targetScreen,
                previousScreen = "",
                duration = 0L,
                workflowId = UUID.randomUUID().toString(),
                success = true,
                metadata = mapOf("features" to metadata?.toString() ?: "").toString()
            )
            db.interactionEventDao().insert(event)
        }
    }

    override suspend fun trackBackNavigation(screen: String, previousScreen: String, metadata: Map<String, String>?) {
        withContext(Dispatchers.IO) {
            val event = RoomInteractionEvent(
                eventId = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                sessionId = UUID.randomUUID().toString(),
                screen = screen,
                action = "back_navigation",
                target = previousScreen,
                previousScreen = previousScreen,
                duration = 0L,
                workflowId = UUID.randomUUID().toString(),
                success = true,
                metadata = mapOf("features" to metadata?.toString() ?: "").toString()
            )
            db.interactionEventDao().insert(event)
        }
    }

    override suspend fun trackDwellTime(screen: String, duration: Long, metadata: Map<String, String>?) {
        withContext(Dispatchers.IO) {
            val event = RoomInteractionEvent(
                eventId = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                sessionId = UUID.randomUUID().toString(),
                screen = screen,
                action = "dwelling",
                target = screen,
                previousScreen = "",
                duration = duration,
                workflowId = UUID.randomUUID().toString(),
                success = true,
                metadata = mapOf("features" to metadata?.toString() ?: "").toString()
            )
            db.interactionEventDao().insert(event)
        }
    }

    override suspend fun trackRepeatedAction(screen: String, action: String, count: Int, metadata: Map<String, String>?) {
        withContext(Dispatchers.IO) {
            val event = RoomInteractionEvent(
                eventId = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                sessionId = UUID.randomUUID().toString(),
                screen = screen,
                action = "repeated_action",
                target = action,
                previousScreen = "",
                duration = 0L,
                workflowId = UUID.randomUUID().toString(),
                success = true,
                metadata = mapOf("features" to count.toString(), "features" to metadata?.toString() ?: "").toString()
            )
            db.interactionEventDao().insert(event)
        }
    }

    override suspend fun trackWorkflowCompletion(screen: String, workflowId: String, success: Boolean, metadata: Map<String, String>?) {
        withContext(Dispatchers.IO) {
            val event = RoomInteractionEvent(
                eventId = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                sessionId = UUID.randomUUID().toString(),
                screen = screen,
                action = "workflow_completion",
                target = workflowId,
                previousScreen = "",
                duration = 0L,
                workflowId = workflowId,
                success = success ? 1 : 0,
                metadata = mapOf("features" to metadata?.toString() ?: "").toString()
            )
            db.interactionEventDao().insert(event)
        }
    }

    override suspend fun trackWorkflowAbandonment(screen: String, workflowId: String, reason: String, metadata: Map<String, String>?) {
        withContext(Dispatchers.IO) {
            val event = RoomInteractionEvent(
                eventId = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                sessionId = UUID.randomUUID().toString(),
                screen = screen,
                action = "workflow_abandonment",
                target = reason,
                previousScreen = "",
                duration = 0L,
                workflowId = workflowId,
                success = 0,
                metadata = mapOf("features" to reason, "features" to metadata?.toString() ?: "").toString()
            )
            db.interactionEventDao().insert(event)
        }
    }

    override suspend fun trackInteractionError(screen: String, action: String, error: String, metadata: Map<String, String>?) {
        withContext(Dispatchers.IO) {
            val event = RoomInteractionEvent(
                eventId = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                sessionId = UUID.randomUUID().toString(),
                screen = screen,
                action = "interaction_error",
                target = error,
                previousScreen = "",
                duration = 0L,
                workflowId = UUID.randomUUID().toString(),
                success = false,
                metadata = mapOf("features" to error, "features" to metadata?.toString() ?: "").toString()
            )
            db.interactionEventDao().insert(event)
        }
    }

    override suspend fun trackTaskCompletionTime(taskId: String, duration: Long, success: Boolean, metadata: Map<String, String>?) {
        withContext(Dispatchers.IO) {
            val event = RoomInteractionEvent(
                eventId = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                sessionId = UUID.randomUUID().toString(),
                screen = "",
                action = "task_completion_time",
                target = taskId,
                previousScreen = "",
                duration = duration,
                workflowId = UUID.randomUUID().toString(),
                success = success ? 1 : 0,
                metadata = mapOf("features" to metadata?.toString() ?: "").toString()
            )
            db.interactionEventDao().insert(event)
        }
    }

    override suspend fun trackFeatureUsageFrequency(feature: String, count: Int, metadata: Map<String, String>?) {
        withContext(Dispatchers.IO) {
            val event = RoomInteractionEvent(
                eventId = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                sessionId = UUID.randomUUID().toString(),
                screen = "",
                action = "feature_usage_frequency",
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
}