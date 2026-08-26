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
import com.dynamic.dynamicbehavioradaptiveui.R

@Composable
fun ExperimentComparisonScreen() {
    val repo = ExperimentRepository()
    val context = remember { ApplicationContext() }

    var selectedMode by remember { mutableStateOf(ExperimentMode.BASELINE) }
    var showImprovements by remember { mutableStateOf(false) }

    val baselineMetrics by repo.experimentMetricsDao().getMetricsByMode(ExperimentMode.BASELINE).collectAsList()
    val adaptiveMetrics by repo.experimentMetricsDao().getMetricsByMode(ExperimentMode.ADAPTIVE).collectAsList()

    val baselineAvgTime = if (baselineMetrics.isNotEmpty()) {
        baselineMetrics.averageBy { it.taskCompletionTimeMs } / 1000.0
    } else 0.0

    val adaptiveAvgTime = if (adaptiveMetrics.isNotEmpty()) {
        adaptiveMetrics.averageBy { it.taskCompletionTimeMs } / 1000.0
    } else 0.0

    val baselineAvgInteractions = if (baselineMetrics.isNotEmpty()) {
        baselineMetrics.averageBy { it.numInteractions }.toDouble()
    } else 0.0

    val adaptiveAvgInteractions = if (adaptiveMetrics.isNotEmpty()) {
        adaptiveMetrics.averageBy { it.numInteractions }.toDouble()
    } else 0.0

    val baselineAvgBacktracking = if (baselineMetrics.isNotEmpty()) {
        baselineMetrics.averageBy { it.backtrackingCount }.toDouble()
    } else 0.0

    val adaptiveAvgBacktracking = if (adaptiveMetrics.isNotEmpty()) {
        adaptiveMetrics.averageBy { it.backtrackingCount }.toDouble()
    } else 0.0

    val baselineAvgErrors = if (baselineMetrics.isNotEmpty()) {
        baselineMetrics.averageBy { it.errorCount }.toDouble()
    } else 0.0

    val adaptiveAvgErrors = if (adaptiveMetrics.isNotEmpty()) {
        adaptiveMetrics.averageBy { it.errorCount }.toDouble()
    } else 0.0

    val featureDiscoverabilityImprovement = if (baselineAvgInteractions > 0 && adaptiveAvgInteractions > 0) {
        ((baselineAvgInteractions - adaptiveAvgInteractions) / baselineAvgInteractions) * 100.0
    } else 0.0

    val backtrackingImprovement = if (baselineAvgBacktracking > 0) {
        ((baselineAvgBacktracking - adaptiveAvgBacktracking) / baselineAvgBacktracking) * 100.0
    } else 0.0

    val errorReduction = if (baselineAvgErrors > 0) {
        ((baselineAvgErrors - adaptiveAvgErrors) / baselineAvgErrors) * 100.0
    } else 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("A/B Experiment Comparison", style = MaterialTypography.titleLarge) },
                actions = {
                    OutlinedButton(onClick = { repo.switchMode() }) {
                        Text("Switch Mode: ${selectedMode.id}")
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text("Experiment Mode: ${selectedMode.id}", style = MaterialTypography.headlineSmall)

            if (showImprovements) {
                ExperimentMetricCard(
                    label: "Task Completion Time",
                    baselineValue: "${formatTime(baselineAvgTime)}",
                    adaptiveValue: "${formatTime(adaptiveAvgTime)}",
                    improvement: "${calculatePercentage(baselineAvgTime, adaptiveAvgTime)}% faster",
                    baselineColor: MaterialTheme.colorScheme.error,
                    adaptiveColor: MaterialTheme.colorScheme.success
                )

                ExperimentMetricCard(
                    label: "Number of Interactions",
                    baselineValue: "${baselineAvgInteractions.format(1)}",
                    adaptiveValue: "${adaptiveAvgInteractions.format(1)}",
                    improvement: "${featureDiscoverabilityImprovement.format(1)}% fewer interactions",
                    baselineColor: MaterialTheme.colorScheme.primary,
                    adaptiveColor: MaterialTheme.colorScheme.onPrimary
                )

                ExperimentMetricCard(
                    label: "Backtracking",
                    baselineValue: "${baselineAvgBacktracking.format(1)}",
                    adaptiveValue: "${adaptiveAvgBacktracking.format(1)}",
                    improvement: "${backtrackingImprovement.format(1)}% reduction",
                    baselineColor: MaterialTheme.colorScheme.warning,
                    adaptiveColor: MaterialTheme.colorScheme.onWarning
                )

                ExperimentMetricCard(
                    label: "Errors",
                    baselineValue: "${baselineAvgErrors.format(1)}",
                    adaptiveValue: "${adaptiveAvgErrors.format(1)}",
                    improvement: "${errorReduction.format(1)}% reduction",
                    baselineColor: MaterialTheme.colorScheme.error,
                    adaptiveColor: MaterialTheme.colorScheme.success
                )
            } else {
                BaselinevsAdaptiveComparison(
                    baselineAvgTime: baselineAvgTime,
                    adaptiveAvgTime: adaptiveAvgTime,
                    baselineAvgInteractions: baselineAvgInteractions,
                    adaptiveAvgInteractions: adaptiveAvgInteractions,
                    onCompareClick = { showImprovements = true }
                )
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
fun ExperimentMetricCard(
    label: String,
    baselineValue: String,
    adaptiveValue: String,
    improvement: String,
    baselineColor: Color,
    adaptiveColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(4.dp),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = MaterialTypography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalAlignment = Alignment.Center
            ) {
                Text(
                    text = "Baseline: $baselineValue",
                    style = MaterialTypography.bodyMedium,
                    color = baselineColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Adaptive: $adaptiveValue",
                    style = MaterialTypography.bodyMedium,
                    color = adaptiveColor
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = improvement,
                style = MaterialTypography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun BaselinevsAdaptiveComparison(
    baselineAvgTime: Double,
    adaptiveAvgTime: Double,
    baselineAvgInteractions: Double,
    adaptiveAvgInteractions: Double,
    onCompareClick: () -> Unit
) {
    var showDetails by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(4.dp),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "A/B Experiment Comparison",
                style = MaterialTypography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalAlignment = Alignment.Center
            ) {
                BaselineMetricItem(
                    label: "Time",
                    baseline: "${formatTime(baselineAvgTime)}",
                    adaptive: "${formatTime(adaptiveAvgTime)}",
                    onSelect = { /* navigate to details */ }
                )
                Spacer(modifier = Modifier.width(8.dp))
                AdaptiveMetricItem(
                    label: "Time",
                    baseline: "${formatTime(baselineAvgTime)}",
                    adaptive: "${formatTime(adaptiveAvgTime)}"
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalAlignment = Alignment.Center
            ) {
                BaselineMetricItem(
                    label: "Interactions",
                    baseline: "${baselineAvgInteractions.format(1)}",
                    adaptive: "${adaptiveAvgInteractions.format(1)}",
                    onSelect = { /* navigate to details */ }
                )
                Spacer(modifier = Modifier.width(8.dp))
                AdaptiveMetricItem(
                    label: "Interactions",
                    baseline: "${baselineAvgInteractions.format(1)}",
                    adaptive: "${adaptiveAvgInteractions.format(1)}"
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { onCompareClick() },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text(if (showDetails) "Show improvements" else "Show percentage improvements", style = MaterialTypography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (showDetails) {
                Text(
                    "Baseline average task completion time: ${formatTime(baselineAvgTime)}s",
                    style = MaterialTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Adaptive average task completion time: ${formatTime(adaptiveAvgTime)}s",
                    style = MaterialTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Percentage improvement: ${calculatePercentage(baselineAvgTime, adaptiveAvgTime).format(1)}% faster",
                    style = MaterialTypography.bodySmall,
                    color = MaterialTheme.colorScheme.success
                )
            }
        }
    }
}

@Composable
fun BaselineMetricItem(
    label: String,
    baseline: String,
    adaptive: String,
    onSelect: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Baseline",
            style = MaterialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = baseline,
            style = MaterialTypography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
        Text(
            text = adaptive,
            style = MaterialTypography.bodyMedium,
            color = MaterialTheme.colorScheme.onError
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedButton(
            onClick = onSelect,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View details", style = MaterialTypography.labelSmall)
        }
    }
}

@Composable
fun AdaptiveMetricItem(
    label: String,
    baseline: String,
    adaptive: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Adaptive",
            style = MaterialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = baseline,
            style = MaterialTypography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = adaptive,
            style = MaterialTypography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

private fun formatTime(ms: Double): String {
    if (ms >= 60000) {
        val minutes = (ms / 60000).toInt()
        val seconds = ((ms % 60000) / 1000).toInt()
        return "$minutes:${seconds.formatted("%02d")}s"
    } else {
        "${ms.toInt()}ms"
    }
}

private fun calculatePercentage(baseline: Double, adaptive: Double): Double {
    if (baseline > 0) {
        return ((baseline - adaptive) / baseline) * 100.0
    }
    return 0.0
}