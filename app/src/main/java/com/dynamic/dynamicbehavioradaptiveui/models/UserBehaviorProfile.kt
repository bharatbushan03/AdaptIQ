package com.dynamic.dynamicbehavioradaptiveui.models

data class UserBehaviorProfile(
    val featureUsageFrequency: Map<String, Int> = emptyMap(),
    val mostFrequentlyUsedFeatures: List<String> = emptyList(),
    val averageDwellTime: Double = 0.0,
    val navigationDepth: Int = 0,
    val backtrackingFrequency: Int = 0,
    val repeatedNavigationPaths: Int = 0,
    val repeatedActions: Map<String, Int> = emptyMap(),
    val workflowCompletionRate: Double = 0.0,
    val workflowAbandonmentRate: Double = 0.0,
    val averageTaskCompletionTime: Double = 0.0,
    val interactionErrorRate: Double = 0.0,
    val attemptsBeforeSuccessfulCompletion: Map<String, Int> = emptyMap(),
    val frequentlyVisitedScreenSequences: List<List<String>> = emptyList()
)