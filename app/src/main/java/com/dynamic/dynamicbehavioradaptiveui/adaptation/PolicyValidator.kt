package com.dynamic.dynamicbehavioradaptiveui.adaptation

import com.dynamic.dynamicbehavioradaptiveui.behavior.BehaviorState
import com.dynamic.dynamicbehavioradaptiveui.models.InteractionEvent

class PolicyValidator private constructor(
    private val maxSimultaneousAdaptations: Int = 3,
    private val confidenceThreshold: Double = 0.6,
    private val stableNavigationTypes: Set<String> = setOf("bottom_bar", "main_menu", "primary_navigation"),
    private val criticalControlTypes: Set<String> = setOf(
        "save",
        "delete",
        "submit",
        "cancel",
        "auth_submit",
        "password",
        "permission_grant"
    )
) {

    companion object {
        fun create(): PolicyValidator = PolicyValidator()
    }

    fun validateRecommendation(
        request: AdaptationRequest,
        recommendation: Adaptation
    ): Boolean {
        if (!_checkConfidence(recommendation)) return false
        if (!_checkReversible(recommendation)) return false
        if (!_checkExpiration(recommendation)) return false
        if (!_checkMaxSimultaneous(request, recommendation)) return false
        if (!_checkPrimaryNavigationStable(request, recommendation)) return false
        if (!_checkCriticalControlsStable(request, recommendation)) return false
        if (!_checkNoDestructiveActions(recommendation)) return false
        if (!_checkNotSecuritySensitive(recommendation)) return false
        return true
    }

    fun validateCanApply(
        currentState: AppState,
        adaptation: Adaptation
    ): Boolean {
        if (currentState.activeAdaptations.size >= maxSimultaneousAdaptations) return false
        if (adaptation.isSecuritySensitive) return false
        if (!adaptation.isReversible) return false
        return true
    }

    private fun _checkConfidence(adaptation: Adaptation): Boolean {
        return adaptation.confidence >= confidenceThreshold
    }

    private fun _checkReversible(adaptation: Adaptation): Boolean {
        return adaptation.isReversible
    }

    private fun _checkExpiration(adaptation: Adaptation): Boolean {
        return System.currentTimeMillis() <= adaptation.expiresAt
    }

    private fun _checkMaxSimultaneous(request: AdaptationRequest, adaptation: Adaptation): Boolean {
        val currentCount = request.interactionEvents.size
        return (request.interactionEvents.size + 1) <= maxSimultaneousAdaptations
    }

    private fun _checkPrimaryNavigationStable(
        request: AdaptationRequest,
        adaptation: Adaptation
    ): Boolean {
        val affectedNavigation = adaptation.description.lowercase()
        return stableNavigationTypes.none { navType ->
            affectedNavigation.contains(navType)
        }
    }

    private fun _checkCriticalControlsStable(
        request: AdaptationRequest,
        adaptation: Adaptation
    ): Boolean {
        val affectedControls = adaptation.description.lowercase()
        return criticalControlTypes.none { control ->
            affectedControls.contains(control)
        }
    }

    private fun _checkNoDestructiveActions(adaptation: Adaptation): Boolean {
        val lowerDesc = adaptation.description.lowercase()
        val destructiveKeywords = setOf(
            "delete", "remove", "destroy", "kill", "exit", "close",
            "terminate", "disable_permanently"
        )
        return !destructiveKeywords.any { lowerDesc.contains(it) }
    }

    private fun _checkNotSecuritySensitive(adaptation: Adaptation): Boolean {
        return !adaptation.isSecuritySensitive
    }