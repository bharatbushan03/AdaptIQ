package com.dynamic.dynamicbehavioradaptiveui.behavior

import com.dynamic.dynamicbehavioradaptiveui.models.InteractionEvent
import com.dynamic.dynamicbehavioradaptiveui.models.UserBehaviorProfile

class BehaviorFeatureExtractor {

    private val featureUsageFrequency = mutableMapOf<String, Int>()
    private val actionFrequency = mutableMapOf<String, Int>()
    private var totalDwellTime: Long = 0
    private var dwellEventCount: Int = 0
    private var maxNavigationDepth: Int = 0
    private var currentNavigationDepth: Int = 0
    private var backtrackFrequency: Int = 0
    private val workflowStates = mutableMapOf<String, WorkflowState>()
    private val screenSequenceCounts = mutableMapOf<List<String>, Int>()
    private var totalInteractions: Int = 0
    private var totalTaskCompletionTime: Long = 0
    private var taskCompletionCount: Int = 0
    private var interactionErrorCount: Int = 0
    private var shortcutUsageCount: Int = 0
    private var adaptationAcceptanceCount: Int = 0
    private var adaptationEffectivenessSum: Double = 0.0

    private data class WorkflowState(
        val hasSuccess: Boolean = false,
        val attemptCount: Int = 0
    )

    fun update(event: InteractionEvent) {
        totalInteractions++

        updateFeatureUsage(event)
        updateActionFrequency(event)
        updateDwellTime(event)
        updateNavigation(event)
        updateBacktracking(event)
        updateWorkflowState(event)
        updateTaskCompletion(event)
        updateInteractionErrors(event)
        updateScreenSequences(event)
        updateShortcutUsage(event)
        updateAdaptationAcceptance(event)
    }

    private fun updateFeatureUsage(event: InteractionEvent) {
        val feature = event.target
        if (feature.isNotEmpty()) {
            featureUsageFrequency[feature] = (featureUsageFrequency[feature] ?: 0) + 1
        }
    }

    private fun updateActionFrequency(event: InteractionEvent) {
        val action = event.action
        if (action.isNotEmpty()) {
            actionFrequency[action] = (actionFrequency[action] ?: 0) + 1
        }
    }

    private fun updateDwellTime(event: InteractionEvent) {
        if (event.duration > 0) {
            totalDwellTime += event.duration
            dwellEventCount++
        }
    }

    private fun updateNavigation(event: InteractionEvent) {
        val currentScreen = event.screen
        val previous = event.previousScreen

        if (currentScreen.isNotEmpty() && previous.isNotEmpty() && currentScreen != previous) {
            currentNavigationDepth++
            if (currentNavigationDepth > maxNavigationDepth) {
                maxNavigationDepth = currentNavigationDepth
            }
            val sequence = listOf(previous, currentScreen)
            screenSequenceCounts[sequence] = (screenSequenceCounts[sequence] ?: 0) + 1
        }

        if (event.action == "back_navigation" || event.action == "back") {
            backtrackFrequency++
            currentNavigationDepth = Math.max(0, currentNavigationDepth - 1)
        }
    }

    private fun updateBacktracking(event: InteractionEvent) {
        if (event.action == "back_navigation" || event.action == "back") {
            backtrackFrequency++
            currentNavigationDepth = Math.max(0, currentNavigationDepth - 1)
        }
    }

    private fun updateWorkflowState(event: InteractionEvent) {
        val workflowId = event.workflowId
        if (workflowId.isEmpty()) return

        val previous = workflowStates[workflowId] ?: WorkflowState()
        val newHasSuccess = previous.hasSuccess || event.success
        val newAttemptCount = previous.attemptCount + 1
        workflowStates[workflowId] = WorkflowState(newHasSuccess, newAttemptCount)
    }

    private fun updateTaskCompletion(event: InteractionEvent) {
        if (event.duration > 0 && event.success) {
            totalTaskCompletionTime += event.duration
            taskCompletionCount++
        }
    }

    private fun updateInteractionErrors(event: InteractionEvent) {
        if (event.success == false) {
            interactionErrorCount++
        }
    }

    private fun updateShortcutUsage(event: InteractionEvent) {
        val action = event.action.lowercase()
        val shortcutActions = listOf(
            "shortcut_used",
            "ctrl_press",
            "cmd_press",
            "quick_action",
            "fast_navigation"
        )
        if (shortcutActions.any { action.startsWith(it) }) {
            shortcutUsageCount++
        }
    }

    private fun updateAdaptationAcceptance(event: InteractionEvent) {
        val action = event.action.lowercase()
        val adaptationActions = listOf(
            "adaptation_accepted",
            "adaptation_applied",
            "feature_enabled",
            "layout_accepted"
        )
        if (adaptationActions.any { action.contains(it) }) {
            adaptationAcceptanceCount++
            adaptationEffectivenessSum += if (event.success) 1.0 else 0.0
        }
    }

    private fun updateScreenSequences(event: InteractionEvent) {
        val currentScreen = event.screen
        val previous = event.previousScreen

        if (currentScreen.isNotEmpty() && previous.isNotEmpty() && currentScreen != previous) {
            val sequence = listOf(previous, currentScreen)
            screenSequenceCounts[sequence] = (screenSequenceCounts[sequence] ?: 0) + 1
        }
    }

    fun getProfile(): UserBehaviorProfile {
        val mostFrequentlyUsed = featureUsageFrequency.entries
            .sortedByDescending { it.value }
            .map { it.key }
            .take(5)
            .toList()

        val repeatedNavigationPaths = screenSequenceCounts.entries
            .filter { it.value > 1 }
            .map { it.key }
            .toList()

        val repeatedActions = actionFrequency.entries
            .filter { it.value > 1 }
            .associate { it.key to it.value }

        val screenSequences = extractTopScreenSequences()

        val workflowCompletionRate = if (workflowStates.isNotEmpty()) {
            val completed = workflowStates.values.count { it.hasSuccess }
            val total = workflowStates.size
            completed.toDouble() / total
        } else 0.0

        val workflowAbandonmentRate = if (workflowStates.isNotEmpty()) {
            val abandoned = workflowStates.values.count { !it.hasSuccess }
            val total = workflowStates.size
            abandoned.toDouble() / total
        } else 0.0

        val averageDwellTime = if (dwellEventCount > 0) totalDwellTime.toDouble() / dwellEventCount else 0.0
        val averageTaskCompletionTime = if (taskCompletionCount > 0) totalTaskCompletionTime.toDouble() / taskCompletionCount else 0.0
        val interactionErrorRate = if (totalInteractions > 0) interactionErrorCount.toDouble() / totalInteractions else 0.0
        val attemptsBeforeSuccessfulCompletion = deriveAttemptsBeforeSuccess()

        return UserBehaviorProfile(
            featureUsageFrequency = featureUsageFrequency.toMap(),
            mostFrequentlyUsedFeatures = mostFrequentlyUsed,
            averageDwellTime = averageDwellTime,
            navigationDepth = maxNavigationDepth,
            backtrackingFrequency = backtrackFrequency,
            repeatedNavigationPaths = repeatedNavigationPaths.size,
            repeatedActions = repeatedActions,
            workflowCompletionRate = workflowCompletionRate,
            workflowAbandonmentRate = workflowAbandonmentRate,
            averageTaskCompletionTime = averageTaskCompletionTime,
            interactionErrorRate = interactionErrorRate,
            attemptsBeforeSuccessfulCompletion = attemptsBeforeSuccessfulCompletion,
            frequentlyVisitedScreenSequences = screenSequences,
            shortcutUsageCount = shortcutUsageCount,
            adaptationAcceptanceCount = adaptationAcceptanceCount,
            adaptationEffectivenessSum = adaptationEffectivenessSum
        )
    }

    private fun deriveAttemptsBeforeSuccess(): Map<String, Int> {
        return workflowStates.entries
            .filter { it.value.hasSuccess }
            .associateKeys { it.key to it.value.attemptCount }
    }

    private fun extractTopScreenSequences(): List<List<String>> {
        return screenSequenceCounts.entries
            .sortedByDescending { it.value }
            .map { it.key }
            .take(5)
            .toList()
    }
}