package com.dynamic.dynamicbehavioradaptiveui.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dynamic.dynamicbehavioradaptiveui.behavior.ExperimentMode
import com.dynamic.dynamicbehavioradaptiveui.behavior.ExperimentRepository
import com.dynamic.dynamicbehavioradaptiveui.models.ExperimentMetrics
import com.dynamic.dynamicbehavioradaptiveui.models.BehaviorState
import com.dynamic.dynamicbehavioradaptiveui.models.InteractionFriction
import com.dynamic.dynamicbehavioradaptiveui.models.ProficiencyLevel
import com.dynamic.dynamicbehavioradaptiveui.models.WorkflowFamiliarity
import com.dynamic.dynamicbehavioradaptiveui.R
import kotlinx.coroutines.flow.collectAsList

@Composable
fun AnalyticsDashboard() {
    val repo = ExperimentRepository()
    val context = remember { ApplicationContext() }
    var selectedMode by remember { mutableStateOf(ExperimentMode.BASELINE) }

    val baselineMetrics by repo.experimentMetricsDao().getAllMetrics().collectAsList()
    val adaptiveMetrics by repo.experimentMetricsDao().getAllMetrics().collectAsList()

    val baselineEvents by repo.interactionEventDao().getAllEvents().collectAsList()
    val adaptiveEvents by repo.interactionEventDao().getAllEvents().collectAsList()

    val behaviorState by remember { repo.behaviorState }

    val mostUsedFeatures by repo.experimentMetricsDao().getMostUsedFeatures(selectedMode).collectAsList()
    val navigationPaths by repo.experimentMetricsDao().getNavigationPaths().collectAsList()

    var showAdaptiveDecisions by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics Dashboard", style = MaterialTypography.titleLarge) },
                actions = {
                    Picker(
                        selected = selectedMode,
                        onSelect = { selectedMode = it },
                        items = ExperimentMode.values(),
                        label = { Text("Mode: ${it.id}") }
                    )
                }
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text("Experiment Mode: ${selectedMode.id}", style = MaterialTypography.headlineSmall)

            MetricGridRow(
                baselineMetrics = baselineMetrics,
                adaptiveMetrics = adaptiveMetrics,
                mostUsedFeatures = mostUsedFeatures,
                navigationPaths = navigationPaths,
                backtrackingFrequency = computeBacktrackingFrequency(selectedMode),
                errorRate = computeErrorRate(selectedMode),
                taskCompletionTime = computeAvgTaskCompletionTime(selectedMode),
                proficiency = computeProficiency(selectedMode),
                friction = computeFriction(selectedMode),
                activeAdaptations = computeActiveAdaptations(selectedMode),
                adaptationEffectiveness = computeAdaptationEffectiveness(selectedMode),
                onAdaptiveDecisionsClick = { showAdaptiveDecisions = true }
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (showAdaptiveDecisions) {
                AdaptiveDecisionsSection(
                    decisions = generateAdaptiveDecisions(selectedMode),
                    onCollapse = { showAdaptiveDecisions = false }
                )
            } else {
                Text("Adaptive decisions hidden. Click to expand.", style = MaterialTypography.bodySmall)
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (baselineMetrics.isNotEmpty() || adaptiveMetrics.isNotEmpty()) {
                Text(
                    "Sessions: ${baselineMetrics.size} baseline | ${adaptiveMetrics.size} adaptive",
                    style = MaterialTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MetricGridRow(
    baselineMetrics: List<ExperimentMetrics>,
    adaptiveMetrics: List<ExperimentMetrics>,
    mostUsedFeatures: List<String>,
    navigationPaths: List<String>,
    backtrackingFrequency: Double,
    errorRate: Double,
    taskCompletionTime: Double,
    proficiency: ProficiencyLevel,
    friction: InteractionFriction,
    activeAdaptations: Int,
    adaptationEffectiveness: Double,
    onAdaptiveDecisionsClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(4.dp), elevation = 2.dp) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            MetricItem(
                label = "User Proficiency",
                value = proficiencyToString(proficiency),
                onClick = onAdaptiveDecisionsClick
            )
            MetricItem(
                label = "Current Intent",
                value = computeCurrentIntent(),
                onClick = onAdaptiveDecisionsClick
            )
            MetricItem(
                label = "Interaction Friction",
                value = frictionToString(friction),
                onClick = onAdaptiveDecisionsClick
            )
            MetricItem(
                label = "Most Used Features",
                value = mostUsedFeatures.isNotEmpty() ? mostUsedFeatures.joinToString(", ") : "—",
                onClick = onAdaptiveDecisionsClick
            )
            MetricItem(
                label = "Common Navigation Paths",
                value = navigationPaths.isNotEmpty() ? "${navigationPaths.size} paths" : "—",
                onClick = onAdaptiveDecisionsClick
            )
            MetricItem(
                label = "Backtracking Frequency",
                value = "${backtrackingFrequency.format(1)}",
                onClick = onAdaptiveDecisionsClick
            )
            MetricItem(
                label = "Task Completion Time",
                value = "${taskCompletionTime.format(1)}s",
                onClick = onAdaptiveDecisionsClick
            )
            MetricItem(
                label = "Error Rate",
                value = "${(errorRate * 100).format(1)}%",
                onClick = onAdaptiveDecisionsClick
            )
            MetricItem(
                label = "Active Adaptations",
                value = activeAdaptations.toString(),
                onClick = onAdaptiveDecisionsClick
            )
            MetricItem(
                label = "Adaptation Effectiveness",
                value = "${(adaptationEffectiveness * 100).format(1)}%",
                onClick = onAdaptiveDecisionsClick
            )
        }
    }
}

@Composable
fun MetricItem(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = label, style = MaterialTypography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalAlignment = Alignment.Center
        ) {
            Text(text = "📊", style = MaterialTypography.displaySmall)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = value, style = MaterialTypography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Details", style = MaterialTypography.labelSmall)
        }
    }
}

@Composable
fun VisualizationSection(
    baselineMetrics: List<ExperimentMetrics>,
    adaptiveMetrics: List<ExperimentMetrics>
) {
    Card(modifier = Modifier.fillMaxWidth().padding(4.dp), elevation = 2.dp) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text("Visualizations", style = MaterialTypography.titleMedium, modifier = Modifier.padding(16.dp))

            val avgBaselineTime = if (baselineMetrics.isNotEmpty()) baselineMetrics.averageBy { it.taskCompletionTimeMs } / 1000.0 else 0.0
            val avgAdaptiveTime = if (adaptiveMetrics.isNotEmpty()) adaptiveMetrics.averageBy { it.taskCompletionTimeMs } / 1000.0 else 0.0

            Row(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                baselineTimeTag(avgBaselineTime, "Baseline")
                adaptiveTimeTag(avgAdaptiveTime, "Adaptive")
            }

            Spacer(modifier = Modifier.height(8.dp))

            baselineClicksTag(if (baselineMetrics.isNotEmpty()) baselineMetrics.averageBy { it.numInteractions }.toDouble() else 0.0)
            adaptiveClicksTag(if (adaptiveMetrics.isNotEmpty()) adaptiveMetrics.averageBy { it.numInteractions }.toDouble() else 0.0)

            Spacer(modifier = Modifier.height(8.dp))

            baselineBacktrackTag(if (baselineMetrics.isNotEmpty()) baselineMetrics.averageBy { it.backtrackingCount }.toDouble() else 0.0)
            adaptiveBacktrackTag(if (adaptiveMetrics.isNotEmpty()) adaptiveMetrics.averageBy { it.backtrackingCount }.toDouble() else 0.0)

            Spacer(modifier = Modifier.height(8.dp))

            baselineErrorsTag(if (baselineMetrics.isNotEmpty()) baselineMetrics.averageBy { it.errorCount }.toDouble() else 0.0)
            adaptiveErrorsTag(if (adaptiveMetrics.isNotEmpty()) adaptiveMetrics.averageBy { it.errorCount }.toDouble() else 0.0)
        }
    }
}

@Composable
fun baselineTimeTag(value: Double, label: String) {
    outlinedTextTag(value, label, MaterialTheme.colorScheme.error)
}

@Composable
fun adaptiveTimeTag(value: Double, label: String) {
    outlinedTextTag(value, label, MaterialTheme.colorScheme.success)
}

@Composable
fun outlinedTextTag(value: Double, label: String, color: Color) {
    Card(modifier = Modifier.padding(4.dp), elevation = 1.dp) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalAlignment = Alignment.Center
            ) {
                Text(text = "$label: ", style = MaterialTypography.bodyMedium, color = color)
                Text(text = "${value.format(1)}s", style = MaterialTypography.bodyMedium, color = color)
            }
            ProgressBar(
                value = value.coerceIn(0.0, 60000.0) / 60000.0,
                modifier = Modifier.width(100.dp)
            )
            Text(text = "Completion time", style = MaterialTypography.caption, color = Color.Gray)
        }
    }
}

@Composable
fun baselineClicksTag(value: Double) {
    textTag("Clicks", "${value.format(1)}", value < 10 ? MaterialTheme.colorScheme.success : MaterialTheme.colorScheme.warning)
}

@Composable
fun adaptiveClicksTag(value: Double) {
    textTag("Clicks", "${value.format(1)}", value < 10 ? MaterialTheme.colorScheme.success : MaterialTheme.colorScheme.warning)
}

@Composable
fun baselineBacktrackTag(value: Double) {
    textTag("Backtracks", "${value.format(1)}", value > 5 ? MaterialTheme.colorScheme.error : MaterialTheme.colorScheme.warning)
}

@Composable
fun adaptiveBacktrackTag(value: Double) {
    textTag("Backtracks", "${value.format(1)}", value > 5 ? MaterialTheme.colorScheme.error : MaterialTheme.colorScheme.warning)
}

@Composable
fun baselineErrorsTag(value: Double) {
    textTag("Errors", "${(value * 100).format(1)}%", MaterialTheme.colorScheme.error)
}

@Composable
fun adaptiveErrorsTag(value: Double) {
    textTag("Errors", "${(value * 100).format(1)}%", MaterialTheme.colorScheme.success)
}

@Composable
fun textTag(label: String, value: String, color: Color) {
    Card(modifier = Modifier.padding(4.dp), elevation = 1.dp) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalAlignment = Alignment.Center
            ) {
                Text(text = "$label: ", style = MaterialTypography.bodyMedium, color = color)
                Text(text = value, style = MaterialTypography.bodyMedium, color = color)
            }
            Text(text = "", style = MaterialTypography.caption)
        }
    }
}

@Composable
fun AdaptiveDecisionsSection(decisions: List<String>, onCollapse: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(4.dp), elevation = 2.dp) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Adaptive Decisions", style = MaterialTypography.titleMedium, modifier = Modifier.padding(16.dp))
            Spacer(modifier = Modifier.height(8.dp))

            for (decision in decisions) {
                Text(decision, style = MaterialTypography.bodyMedium, modifier = Modifier.padding(8.dp))
                Divider(modifier = Modifier.height(1.dp).padding(4.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onCollapse,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text("Collapse decisions", style = MaterialTypography.bodyMedium)
            }
        }
    }
}

private fun detectBehavior(mode: ExperimentMode): String {
    return when (mode) {
        ExperimentMode.BASELINE -> "Baseline mode: Standard UI presented"
        ExperimentMode.ADAPTIVE -> "Adaptive mode: UI personalizing based on behavior"
        else -> "Unknown mode"
    }
}

private fun inferIntent(mode: ExperimentMode): String {
    val metrics = if (mode == ExperimentMode.BASELINE) baselineMetrics else adaptiveMetrics
    val backtracking = metrics.averageBy { it.backtrackingCount }
    val errors = metrics.averageBy { it.errorCount }

    return when {
        backtracking > 5 -> "Repeated workflow detected - user struggling with task flow"
        errors > 0.3 -> "High error rate - UI adapting to reduce friction"
        else -> "Normal operation - maintaining current UI state"
    }
}

private fun adaptationResult(mode: ExperimentMode): String {
    val baseline = baselineMetrics.isNotEmpty() ? baselineMetrics.averageBy { it.taskCompletionTimeMs } / 1000.0 : 0.0
    val adaptive = adaptiveMetrics.isNotEmpty() ? adaptiveMetrics.averageBy { it.taskCompletionTimeMs } / 1000.0 : 0.0

    val improvement = if (baseline > 0) ((baseline - adaptive) / baseline * 100.0).format(1) else 0.0

    return "Task completion ${if (improvement.toDouble() > 0) "improved" else "declined"} by ${improvement}%"
}

fun computeProficiency(mode: ExperimentMode): ProficiencyLevel {
    val metrics = if (mode == ExperimentMode.BASELINE) baselineMetrics else adaptiveMetrics
    if (metrics.isEmpty()) return ProficiencyLevel.INTERMEDIATE

    val avgErrors = metrics.averageBy { it.errorCount }
    val avgInteractions = metrics.averageBy { it.numInteractions }

    return when {
        avgInteractions < 5 || avgErrors > 0.5 -> ProficiencyLevel.BEGINNER
        avgInteractions >= 20 && avgErrors < 0.1 -> ProficiencyLevel.ADVANCED
        else -> ProficiencyLevel.INTERMEDIATE
    }
}

fun computeFriction(mode: ExperimentMode): InteractionFriction {
    val metrics = if (mode == ExperimentMode.BASELINE) baselineMetrics else adaptiveMetrics
    if (metrics.isEmpty()) return InteractionFriction.MEDIUM

    val avgBacktracking = metrics.averageBy { it.backtrackingCount }
    val avgErrors = metrics.averageBy { it.errorCount }

    val backtrackHigh = avgBacktracking >= 10
    val errorHigh = avgErrors >= 0.3

    return when {
        backtrackHigh && errorHigh -> InteractionFriction.HIGH
        avgBacktracking <= 3 && avgErrors <= 0.1 -> InteractionFriction.LOW
        else -> InteractionFriction.MEDIUM
    }
}

fun computeBacktrackingFrequency(mode: ExperimentMode): Double {
    val metrics = if (mode == ExperimentMode.BASELINE) baselineMetrics else adaptiveMetrics
    if (metrics.isNotEmpty()) {
        return metrics.averageBy { it.backtrackingCount }.toDouble()
    }
    return 0.0
}

fun computeErrorRate(mode: ExperimentMode): Double {
    val metrics = if (mode == ExperimentMode.BASELINE) baselineMetrics else adaptiveMetrics
    if (metrics.isNotEmpty()) {
        return metrics.averageBy { it.errorCount }.toDouble()
    }
    return 0.0
}

fun computeAvgTaskCompletionTime(mode: ExperimentMode): Double {
    val metrics = if (mode == ExperimentMode.BASELINE) baselineMetrics else adaptiveMetrics
    if (metrics.isNotEmpty()) {
        return metrics.averageBy { it.taskCompletionTimeMs } / 1000.0
    }
    return 0.0
}

fun computeActiveAdaptations(mode: ExperimentMode): Int {
    return if (mode == ExperimentMode.ADAPTIVE) adaptiveMetrics.size else 0
}

fun computeAdaptationEffectiveness(mode: ExperimentMode): Double {
    val baseline = baselineMetrics.isNotEmpty() ? baselineMetrics.averageBy { it.taskCompletionTimeMs } / 1000.0 : 0.0
    val adaptive = adaptiveMetrics.isNotEmpty() ? adaptiveMetrics.averageBy { it.taskCompletionTimeMs } / 1000.0 : 0.0

    return if (baseline > 0) (1.0 - adaptive / baseline) else 0.0
}

fun proficiencyToString(proficiency: ProficiencyLevel): String {
    return when (proficiency) {
        ProficiencyLevel.BEGINNER -> "Beginner"
        ProficiencyLevel.INTERMEDIATE -> "Intermediate"
        ProficiencyLevel.ADVANCED -> "Advanced"
    }
}

fun frictionToString(friction: InteractionFriction): String {
    return when (friction) {
        InteractionFriction.LOW -> "Low"
        InteractionFriction.MEDIUM -> "Medium"
        InteractionFriction.HIGH -> "High"
    }
}

fun computeCurrentIntent(): String {
    val allEvents = baselineEvents + adaptiveEvents
    val backtrackingCount = allEvents.count { it.action.contains("back_navigation") }

    return when {
        backtrackingCount > 5 -> "repeated_workflow"
        backtrackingCount > 2 -> "in_progress_workflow"
        else -> "general_interaction"
    }
}

fun generateAdaptiveDecisions(mode: ExperimentMode): List<String> {
    val behavior = detectBehavior(mode)
    val intent = inferIntent(mode)
    val result = adaptationResult(mode)

    return@generateAdaptiveDecisions listOf(
        "Behavior detected: $behavior",
        "Inference: $intent",
        "Adaptation: UI adjusted based on inferred behavior",
        "Result: $result"
    )
}