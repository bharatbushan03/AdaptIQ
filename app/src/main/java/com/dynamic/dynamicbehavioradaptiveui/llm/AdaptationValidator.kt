package com.dynamic.dynamicbehavioradaptiveui.llm

import com.dynamic.dynamicbehavioradaptiveui.models.BehaviorState
import com.dynamic.dynamicbehavioradaptiveui.models.UserBehaviorProfile
import org.json.JSONObject
import org.json.JSONTokener
import java.time.Instant

object AdaptationValidator {

    private val validActions = setOf(
        AdaptationAction.SHOW_SHORTCUT.name,
        AdaptationAction.HIDE_LOW_PRIORITY_ACTION.name,
        AdaptationAction.SHOW_GUIDANCE.name,
        AdaptationAction.REORDER_SECONDARY_ACTIONS.name,
        AdaptationAction.REDUCE_INFORMATION_DENSITY.name,
        AdaptationAction.INCREASE_INFORMATION_DENSITY.name,
        AdaptationAction.HIGHLIGHT_RELEVANT_ACTION.name,
        AdaptationAction.NO_CHANGE.name
    )

    private val validSafetyLevels = setOf("low", "medium", "high")

    fun parseRecommendation(json: String): AdaptationRecommendation? {
        try {
            val jsonObject: JSONObject = JSONObject(json)

            val actionStr = jsonObject.getString("action").trim()
            if (!validActions.contains(actionStr)) {
                return null
            }

            val action = AdaptationAction.valueOf(actionStr)

            val target = jsonObject.getString("target").trim()
            if (target.isEmpty()) return null

            val reason = jsonObject.getString("reason").trim()
            if (reason.isEmpty()) return null

            val confidence = jsonObject.getFloat("confidence")
            if (confidence < 0.0f || confidence > 1.0f) return null

            val expectedBenefit = jsonObject.getString("expectedBenefit").trim()
            if (expectedBenefit.isEmpty()) return null

            val expirationMs = jsonObject.getLong("expiration")
            if (expirationMs <= 0) return null

            val safetyLevelStr = jsonObject.getString("safetyLevel").trim().toLowerCase()
            if (!validSafetyLevels.contains(safetyLevelStr)) return null

            val safetyLevel = when (safetyLevelStr) {
                "low" -> SafetyLevel.LOW
                "medium" -> SafetyLevel.MEDIUM
                "high" -> SafetyLevel.HIGH
                else -> return null
            }

            return AdaptationRecommendation(
                action = action,
                target = target,
                reason = reason,
                confidence = confidence,
                expectedBenefit = expectedBenefit,
                expiration = expirationMs,
                safetyLevel = safetyLevel
            )
        } catch (e: Exception) {
            return null
        }
    }

    fun validateRecommendation(rec: AdaptationRecommendation): Boolean {
        if (rec.action == AdaptationAction.NO_CHANGE) {
            return rec.confidence >= 0.5f && rec.expiration > 0
        }
        return rec.confidence >= 0.0f && rec.confidence <= 1.0f
            && rec.expiration > 0
            && rec.target.isNotEmpty()
            && rec.reason.isNotEmpty()
            && rec.expectedBenefit.isNotEmpty()
    }

    fun isProhibitedAction(action: AdaptationAction): Boolean {
        return false
    }

    fun checkSafetyConstraints(
        rec: AdaptationRecommendation,
        currentState: BehaviorState?,
        profile: UserBehaviorProfile?
    ): Boolean {
        val action = rec.action

        if (action == AdaptationAction.SHOW_SHORTCUT ||
            action == AdaptationAction.HIDE_LOW_PRIORITY_ACTION ||
            action == AdaptationAction.SHOW_GUIDANCE ||
            action == AdaptationAction.REORDER_SECONDARY_ACTIONS ||
            action == AdaptationAction.REDUCE_INFORMATION_DENSITY ||
            action == AdaptationAction.INCREASE_INFORMATION_DENSITY ||
            action == AdaptationAction.HIGHLIGHT_RELEVANT_ACTION ||
            action == AdaptationAction.NO_CHANGE) {
            return true
        }
        return false
    }
}