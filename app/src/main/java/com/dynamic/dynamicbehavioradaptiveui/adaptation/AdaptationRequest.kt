package com.dynamic.dynamicbehavioradaptiveui.adaptation

import com.dynamic.dynamicbehavioradaptiveui.behavior.BehaviorState
import com.dynamic.dynamicbehavioradaptiveui.llm.LocalLLM
import com.dynamic.dynamicbehavioradaptiveui.models.BehaviorState

data class AdaptationRequest(
    val sessionId: String,
    val currentBehaviorState: BehaviorState,
    val interactionEvents: List<InteractionEvent>,
    val availableAdaptationTypes: List<String> = emptyList()
)

data class Adaptation(
    val id: String = java.util.UUID.randomUUID().toString(),
    val `type`: AdaptationType,
    val description: String,
    val applyFn: (AppState) -> AppState,
    val revertFn: (AppState) -> AppState,
    val confidence: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val expiresAt: Long,
    val isReversible: Boolean = true,
    val isSecuritySensitive: Boolean = false
)

enum class AdaptationType {
    LAYOUT_ADJUSTMENT,
    THEME_CUSTOMIZATION,
    FEATURE_VISIBILITY,
    NAVIGATION_ENHANCEMENT,
    WIDGET_REORDERING
}

data class AdaptationResult(
    val adaptation: Adaptation,
    val success: Boolean,
    val message: String,
    val preState: AppState,
    val postState: AppState?,
    val appliedAt: Long = System.currentTimeMillis()
)

data class AppState(
    val primaryNavigation: String = "bottom_bar",
    val criticalControls: Set<String> = emptySet(),
    val activeAdaptations: List<Adaptation> = emptyList(),
    val sessionId: String = "",
    val metrics: AdaptationMetrics = AdaptationMetrics()
)