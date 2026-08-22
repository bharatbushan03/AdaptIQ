package com.dynamic.dynamicbehavioradaptiveui.behavior

import com.dynamic.dynamicbehavioradaptiveui.models.BehaviorState
import com.dynamic.dynamicbehavioradaptiveui.models.ProficiencyLevel
import com.dynamic.dynamicbehavioradaptiveui.models.InteractionFriction
import com.dynamic.dynamicbehavioradaptiveui.models.WorkflowFamiliarity
import com.dynamic.dynamicbehavioradaptiveui.models.UserBehaviorProfile

class BehaviorStateEngine {

    data class Metrics(
        val backtrackingFrequency: Int = 0,
        val averageDwellTime: Long = 0L,
        val interactionErrorRate: Double = 0.0,
        val workflowCompletionRate: Double = 0.0,
        val workflowAbandonmentRate: Double = 0.0,
        val mostFrequentlyUsedFeatures: List<String> = emptyList(),
        val repeatedActions: Int = 0,
        val frequentlyVisitedScreenSequences: Int = 0
    )

    private val metrics = mutableMapOf<String, Metrics>()

    private val proficiencyThresholds = ProficiencyThresholds()
    private val frictionThresholds = FrictionThresholds()
    private val familiarityThresholds = FamiliarityThresholds()

    data class ProficiencyThresholds(
        val errorRateMaxForBeginner: Double = 0.3,
        val successRateMinForAdvanced: Double = 0.8,
        val minInteractionsForProficiency: Int = 10
    )

    data class FrictionThresholds(
        val backtrackingMaxForLow: Int = 3,
        val backtrackingMinForHigh: Int = 10,
        val dwellTimeMaxForLowMs: Long = 3000L,
        val dwellTimeMinForHighMs: Long = 8000L,
        val errorRateMaxForLow: Double = 0.1,
        val errorRateMinForHigh: Double = 0.3
    )

    data class FamiliarityThresholds(
        val minFeatureRepeatsForFamiliar: Int = 3,
        val minFeatureRepeatsForFrequent: Int = 10,
        val minSequenceRepeatsForFamiliar: Int = 2,
        val minSequenceRepeatsForFrequent: Int = 5
    )

    init {
        metrics["default"] = Metrics()
    }

    fun updateState(sessionId: String, metrics: Metrics) {
        this.metrics[sessionId] = metrics
    }

    fun getCurrentState(sessionId: String): BehaviorState {
        val m = metrics[sessionId]?.let { it } ?: Metrics()

        val proficiency = inferProficiency(m)
        val friction = inferFriction(m)
        val intent = inferCurrentIntent(m)
        val familiarity = inferWorkflowFamiliarity(m)

        return BehaviorState(
            proficiencyLevel = proficiency,
            interactionFriction = friction,
            currentIntent = intent,
            workflowFamiliarity = familiarity
        )
    }

    private fun inferProficiency(m: Metrics): ProficiencyLevel {
        val errorRate = m.interactionErrorRate
        val repeatActions = m.repeatedActions
        val featureCount = m.mostFrequentlyUsedFeatures.size

        if (featureCount < proficiencyThresholds.minInteractionsForProficiency) {
            return ProficiencyLevel.BEGINNER
        }

        if (errorRate <= proficiencyThresholds.errorRateMaxForBeginner && repeatActions > 5) {
            return ProficiencyLevel.ADVANCED
        }

        if (errorRate > 0.5 || repeatActions < 2) {
            return ProficiencyLevel.BEGINNER
        }

        return ProficiencyLevel.INTERMEDIATE
    }

    private fun inferFriction(m: Metrics): InteractionFriction {
        val backtrack = m.backtrackingFrequency
        val dwellTime = m.averageDwellTime
        val errorRate = m.interactionErrorRate

        val backtrackingLow = backtrack <= frictionThresholds.backtrackingMaxForLow
        val backtrackingHigh = backtrack >= frictionThresholds.backtrackingMinForHigh

        val dwellLow = dwellTime <= frictionThresholds.dwellTimeMaxForLowMs
        val dwellHigh = dwellTime >= frictionThresholds.dwellTimeMinForHighMs

        val errorLow = errorRate <= frictionThresholds.errorRateMaxForLow
        val errorHigh = errorRate >= frictionThresholds.errorRateMinForHigh

        val highFriction = backtrackingHigh && dwellHigh && errorHigh
        val lowFriction = backtrackingLow && dwellLow && errorLow

        if (highFriction) return InteractionFriction.HIGH
        if (lowFriction) return InteractionFriction.LOW
        return InteractionFriction.MEDIUM
    }

    private fun inferCurrentIntent(m: Metrics): String {
        val sequences = m.frequentlyVisitedScreenSequences

        if (sequences >= 5) {
            return "repeated_workflow"
        }

        if (sequences >= 2) {
            return "in_progress_workflow"
        }

        if (m.repeatedActions > 3) {
            return "exploring_features"
        }

        return "general_interaction"
    }

    private fun inferWorkflowFamiliarity(m: Metrics): WorkflowFamiliarity {
        val featureRepeats = m.repeatedActions
        val sequenceRepeats = m.frequentlyVisitedScreenSequences

        if (featureRepeats >= familiarityThresholds.minFeatureRepeatsForFrequent ||
            sequenceRepeats >= familiarityThresholds.minSequenceRepeatsForFrequent) {
            return WorkflowFamiliarity.FREQUENT
        }

        if (featureRepeats >= familiarityThresholds.minFeatureRepeatsForFamiliar ||
            sequenceRepeats >= familiarityThresholds.minSequenceRepeatsForFamiliar) {
            return WorkflowFamiliarity.FAMILIAR
        }

        return WorkflowFamiliarity.NEW
    }
}