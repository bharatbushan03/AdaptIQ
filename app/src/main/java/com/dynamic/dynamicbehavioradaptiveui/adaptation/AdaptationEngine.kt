package com.dynamic.dynamicbehavioradaptiveui.adaptation

import com.dynamic.dynamicbehavioradaptiveui.behavior.BehaviorTracker
import com.dynamic.dynamicbehavioradaptiveui.llm.LocalLLM

// Mock adaptation engine placeholder - no actual AI adaptation yet
class AdaptationEngine(
    private val behaviorTracker: BehaviorTracker,
    private val localLLM: LocalLLM
) {
    suspend fun adaptUI() {
        // Mock adaptation logic - will be implemented with real AI later
    }
}