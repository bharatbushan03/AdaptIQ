package com.dynamic.dynamicbehavioradaptiveui.llm

import com.dynamic.dynamicbehavioradaptiveui.models.BehaviorState
import com.dynamic.dynamicbehavioradaptiveui.models.UserBehaviorProfile

data class AdaptationRecommendation(
    val action: AdaptationAction,
    val target: String,
    val reason: String,
    val confidence: Float,
    val expectedBenefit: String,
    val expiration: Long,
    val safetyLevel: SafetyLevel
)

enum class AdaptationAction {
    SHOW_SHORTCUT,
    HIDE_LOW_PRIORITY_ACTION,
    SHOW_GUIDANCE,
    REORDER_SECONDARY_ACTIONS,
    REDUCE_INFORMATION_DENSITY,
    INCREASE_INFORMATION_DENSITY,
    HIGHLIGHT_RELEVANT_ACTION,
    NO_CHANGE
}

data class SafetyLevel(
    val level: String,
    val description: String
)

object SafetyLevel {
    val LOW = SafetyLevel("low", "Minor UI changes, no impact on functionality")
    val MEDIUM = SafetyLevel("medium", "Moderate UI changes, safe with monitoring")
    val HIGH = SafetyLevel("high", "Restrictive changes, requires user confirmation")
}