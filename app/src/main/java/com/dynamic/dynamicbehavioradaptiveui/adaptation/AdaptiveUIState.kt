package com.dynamic.dynamicbehavioradaptiveui.adaptation

import androidx.compose.runtime.*
import com.dynamic.dynamicbehavioradaptiveui.behavior.BehaviorState
import com.dynamic.dynamicbehavioradaptiveui.models.BehaviorState
import com.dynamic.dynamicbehavioradaptiveui.models.InteractionEvent

@Stable
data class AdaptiveUIState(
    val behaviorState: BehaviorState = BehaviorState(),
    val currentAppState: AppState = AppState(),
    val pendingAdaptation: Adaptation? = null,
    val adaptationHistory: List<AdaptationResult> = emptyList(),
    val isAdapting: Boolean = false,
    val lastAdaptationTime: Long = 0L,
    val workflowImprovementTracked: Boolean = false
)

object AdaptiveUIState {
    fun empty(): AdaptiveUIState {
        return AdaptiveUIState()
    }
}