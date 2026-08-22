package com.dynamic.dynamicbehavioradaptiveui.behavior

import com.dynamic.dynamicbehavioradaptiveui.models.InteractionEvent

interface BehaviorTracker {
    suspend fun trackScreenOpened(screen: String, previousScreen: String?, duration: Long?, metadata: Map<String, String>?)
    suspend fun trackButtonClicked(screen: String, target: String, action: String, metadata: Map<String, String>?)
    suspend fun trackFeatureSelected(screen: String, feature: String, target: String, metadata: Map<String, String>?)
    suspend fun trackNavigation(screen: String, targetScreen: String, action: String, metadata: Map<String, String>?)
    suspend fun trackBackNavigation(screen: String, previousScreen: String, metadata: Map<String, String>?)
    suspend fun trackDwellTime(screen: String, duration: Long, metadata: Map<String, String>?)
    suspend fun trackRepeatedAction(screen: String, action: String, count: Int, metadata: Map<String, String>?)
    suspend fun trackWorkflowCompletion(screen: String, workflowId: String, success: Boolean, metadata: Map<String, String>?)
    suspend fun trackWorkflowAbandonment(screen: String, workflowId: String, reason: String, metadata: Map<String, String>?)
    suspend fun trackInteractionError(screen: String, action: String, error: String, metadata: Map<String, String>?)
    suspend fun trackTaskCompletionTime(taskId: String, duration: Long, success: Boolean, metadata: Map<String, String>?)
    suspend fun trackFeatureUsageFrequency(feature: String, count: Int, metadata: Map<String, String>?)
}