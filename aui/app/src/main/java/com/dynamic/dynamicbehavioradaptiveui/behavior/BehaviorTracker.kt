package com.dynamic.dynamicbehavioradaptiveui.behavior

import com.dynamic.dynamicbehavioradaptiveui.models.InteractionEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Scope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant

interface BehaviorTracker {

    suspend fun trackScreenOpened(
        screen: String,
        sessionId: String?,
        previousScreen: String?,
        workflowId: String?
    )

    suspend fun trackButtonClicked(
        screen: String,
        buttonId: String,
        action: String,
        metadata: String?
    )

    suspend fun trackFeatureUsageFrequency(
        feature: String,
        frequency: Int,
        metadata: String?
    )

    suspend fun trackTaskCompletionTime(
        task: String,
        durationMs: Long,
        success: Boolean,
        metadata: String?
    )

    suspend fun trackInteractionEvent(event: InteractionEvent)

    suspend fun trackInteractionError(
        screen: String,
        error: String,
        metadata: String?
    )

    suspend fun trackBackNavigation(
        screen: String,
        targetScreen: String,
        metadata: String?
    )

    suspend fun trackSessionStart(
        mode: String,
        sessionId: String
    )

    suspend fun trackSessionEnd(
        sessionId: String,
        durationMs: Long,
        metrics: Map<String, Double>
    )

    val interactionEvents: Flow<List<InteractionEvent>>

    val sessionStats: Flow<Pair<Double, Double>>
}