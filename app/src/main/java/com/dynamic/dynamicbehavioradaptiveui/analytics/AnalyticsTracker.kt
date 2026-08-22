package com.dynamic.dynamicbehavioradaptiveui.analytics

// Mock analytics placeholder - no cloud backend, local-only tracking
class AnalyticsTracker {
    private val sharedPreferences = null // Will be integrated with DataStore later

    fun trackScreenVisit(screenName: String) {
        // Local tracking only
    }

    fun trackTaskCompletion(taskId: String) {
        // Local tracking only
    }

    fun trackFeatureUsage(featureName: String) {
        // Local tracking only
    }
}