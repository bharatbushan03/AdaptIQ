package com.dynamic.dynamicbehavioradaptiveui.adaptation

import com.dynamic.dynamicbehavioradaptiveui.models.AdaptationMetrics
import com.dynamic.dynamicbehavioradaptiveui.models.AdaptationOutcome
import com.dynamic.dynamicbehavioradaptiveui.models.AdaptationOutcomeDetermination

class AdaptationScorer {

    private val improvementThreshold = 0.15
    private val deteriorationThreshold = -0.10
    private val minimumInteractionCountForEvaluation = 5

    fun scoreOutcome(
        preMetrics: AdaptationMetrics,
        postMetrics: AdaptationMetrics,
        preTaskCompletionTime: Long?,
        postTaskCompletionTime: Long?,
        taskCompletedAfter: Boolean,
        userInteractionsAfter: Int,
        adaptationAccepted: Boolean
    ): AdaptationOutcomeDetermination {
        val preScore = calculateEffectivenessScore(preMetrics)
        val postScore = calculateEffectivenessScore(postMetrics)

        val taskImproved = taskCompletedAfter &&
            postTaskCompletionTime != null &&
            preTaskCompletionTime != null &&
            postTaskCompletionTime < preTaskCompletionTime

        val metricsImproved = postScore > preScore + improvementThreshold
        val metricsDeteriorated = postScore < preScore + deteriorationThreshold

        val interactionCount = userInteractionsAfter

        return when {
            interactionCount < minimumInteractionCountForEvaluation -> AdaptationOutcomeDetermination.EXPIRE
            !adaptationAccepted -> AdaptationOutcomeDetermination.REVERT
            taskImproved && metricsImproved -> AdaptationOutcomeDetermination.CONTINUE
            taskImproved && !metricsImproved -> AdaptationOutcomeDetermination.MODIFY
            metricsDeteriorated -> AdaptationOutcomeDetermination.REVERT
            postScore <= preScore -> AdaptationOutcomeDetermination.MODIFY
            else -> AdaptationOutcomeDetermination.CONTINUE
        }
    }

    private fun calculateEffectivenessScore(metrics: AdaptationMetrics): Double {
        val completionScore = if (metrics.taskCompletionTime > 0) 1.0 / (1.0 + metrics.taskCompletionTime / 1000.0) else 0.5
        val clickScore = 1.0 - min(metrics.numberOfClicks / 50.0, 1.0)
        val navScore = 1.0 - min(metrics.navigationDepth / 20.0, 1.0)
        val backtrackPenalty = max(0.0, 1.0 - metrics.backtracking * 0.1)
        val errorPenalty = max(0.0, 1.0 - metrics.errors * 0.1)
        val shortcutBonus = min(metrics.shortcutUsage / 10.0, 1.0) * 0.1

        val baseScore = listOf(completionScore, clickScore, navScore, backtrackPenalty, errorPenalty).average()
        return coerceIn(baseScore + shortcutBonus, 0.0, 1.0)
    }

    fun shouldContinue(outcome: AdaptationOutcome): Boolean {
        return outcome.outcome == AdaptationOutcomeDetermination.CONTINUE
    }

    fun shouldModify(outcome: AdaptationOutcome): Boolean {
        return outcome.outcome == AdaptationOutcomeDetermination.MODIFY
    }

    fun shouldExpire(outcome: AdaptationOutcome): Boolean {
        return outcome.outcome == AdaptationOutcomeDetermination.EXPIRE
    }

    fun shouldRevert(outcome: AdaptationOutcome): Boolean {
        return outcome.outcome == AdaptationOutcomeDetermination.REVERT
    }
}