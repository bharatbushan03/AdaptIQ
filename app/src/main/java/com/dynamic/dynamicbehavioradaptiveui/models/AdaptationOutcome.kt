package com.dynamic.dynamicbehavioradaptiveui.models

import java.util.UUID
import java.time.Instant

data class AdaptationOutcome(
    val outcomeId: String = UUID.randomUUID().toString(),
    val adaptationId: String,
    val triggerReason: String,
    val uiChange: String,
    val confidence: Double,
    val timestamp: Long = Instant.now().toEpochMilli(),
    val sessionId: String,
    val preAdaptationMetrics: AdaptationMetrics,
    val postAdaptationMetrics: AdaptationMetrics?,
    val taskCompletionTimeBefore: Long?,
    val taskCompletionTimeAfter: Long?,
    val taskCompletedAfter: Boolean,
    val userInteractionsAfter: List<InteractionEvent>,
    val outcome: AdaptationOutcomeDetermination,
    val effectivenessScore: Double,
    val notes: String = ""
)

data class AdaptationMetrics(
    val taskCompletionTime: Long = 0L,
    val numberOfClicks: Int = 0,
    val navigationDepth: Int = 0,
    val backtracking: Int = 0,
    val errors: Int = 0,
    val workflowAbandonment: Boolean = false,
    val shortcutUsage: Int = 0,
    val adaptationAcceptance: Boolean = false,
    val adaptationEffectiveness: Double = 0.0
)

enum class AdaptationOutcomeDetermination {
    CONTINUE,
    MODIFY,
    EXPIRE,
    REVERT
}