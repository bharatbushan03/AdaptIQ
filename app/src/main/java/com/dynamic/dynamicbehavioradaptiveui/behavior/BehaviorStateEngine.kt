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
    private val coldStartStates = mutableMapOf<String, ColdStartState>()
    @Volatile
    private var forceState: BehaviorState? = null
    @Volatile
    private var forcePhase: String? = null

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

    data class ColdStartThresholds(
        val minInteractionsForPhase2: Int = 5,
        val minInteractionsForPhase3: Int = 20,
        val confidenceAccumulationThreshold: Double = 0.5,
        val decayHalfLifeMillis: Long = 600000L
    )

    data class AdaptationPhase(
        val phase: ColdStartPhase,
        val confidence: Double,
        val accumulatedInteractions: Int
    )

    enum class ColdStartPhase {
        PHASE_1_STABLE_DEFAULT,
        PHASE_2_CONSERVATIVE_SHORTCUTS,
        PHASE_3_PERSONALIZED_ADAPTIVE
    }

    init {
        metrics["default"] = Metrics()
    }

    data class ColdStartState(
        val interactionCount: Int = 0,
        val confidenceAccumulated: Double = 0.0,
        val lastInteractionTime: Long = 0L
    )

    fun updateState(sessionId: String, metrics: Metrics, interactionTimestamp: Long = System.currentTimeMillis()) {
        val existing = metrics[sessionId] ?: Metrics()
        this.metrics[sessionId] = metrics

        val coldStart = coldStartStates[sessionId]
        val updatedCount = (coldStart.interactionCount + 1).coerceAtLeast(0)
        val elapsedSinceLast = interactionTimestamp - coldStart.lastInteractionTime
        val decayFactor = if (elapsedSinceLast > 0) {
            Math.pow(0.5, elapsedSinceLast / coldStart.decayHalfLifeMillis)
        } else {
            1.0
        }
        val decayedConfidence = coldStart.confidenceAccumulated * decayFactor
        val newConfidence = decayedConfidence + 0.1

        coldStartStates[sessionId] = coldStart.copy(
            interactionCount = updatedCount,
            confidenceAccumulated = newConfidence,
            lastInteractionTime = interactionTimestamp
        )
    }

    fun getCurrentState(sessionId: String): BehaviorState {
        if (forceState != null) return forceState!!

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

    fun getCurrentAdaptationPhase(sessionId: String): AdaptationPhase {
        if (forcePhase != null) {
            val phaseWhen = when (forcePhase) {
                "phase_1" -> ColdStartPhase.PHASE_1_STABLE_DEFAULT
                "phase_2" -> ColdStartPhase.PHASE_2_CONSERVATIVE_SHORTCUTS
                "phase_3" -> ColdStartPhase.PHASE_3_PERSONALIZED_ADAPTIVE
                else -> ColdStartPhase.PHASE_1_STABLE_DEFAULT
            }
            return AdaptationPhase(phase = phaseWhen, confidence = 1.0, accumulatedInteractions = 0)
        }

        val coldStart = coldStartStates[sessionId]
        val interactionCount = coldStart.interactionCount
        val confidence = coldStart.confidenceAccumulated

        val phase: ColdStartPhase
        when {
            interactionCount < coldStartThresholds.minInteractionsForPhase2 -> {
                phase = ColdStartPhase.PHASE_1_STABLE_DEFAULT
            }
            interactionCount < coldStartThresholds.minInteractionsForPhase3 -> {
                phase = if (confidence >= coldStartThresholds.confidenceAccumulationThreshold) {
                    ColdStartPhase.PHASE_2_CONSERVATIVE_SHORTCUTS
                } else {
                    ColdStartPhase.PHASE_1_STABLE_DEFAULT
                }
            }
            else -> {
                phase = ColdStartPhase.PHASE_3_PERSONALIZED_ADAPTIVE
            }
        }

        return AdaptationPhase(phase = phase, confidence = confidence, accumulatedInteractions = interactionCount)
    }

    fun resetColdStart(sessionId: String) {
        coldStartStates[sessionId] = ColdStartState(
            interactionCount = 0,
            confidenceAccumulated = 0.0,
            lastInteractionTime = System.currentTimeMillis()
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