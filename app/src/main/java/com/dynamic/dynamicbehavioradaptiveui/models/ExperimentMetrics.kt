package com.dynamic.dynamicbehavioradaptiveui.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "experiment_metrics")
data class ExperimentMetrics(
    @PrimaryKey @ColumnInfo(name = "metrics_id") val metricsId: String,
    @ColumnInfo(name = "session_id") val sessionId: String,
    @ColumnInfo(name = "task_id") val taskId: String,
    @ColumnInfo(name = "mode") val mode: ExperimentMode,
    @ColumnInfo(name = "task_completion_time_ms") val taskCompletionTimeMs: Long,
    @ColumnInfo(name = "num_interactions") val numInteractions: Int,
    @ColumnInfo(name = "navigation_depth") val navigationDepth: Int,
    @ColumnInfo(name = "backtracking_count") val backtrackingCount: Int,
    @ColumnInfo(name = "error_count") val errorCount: Int,
    @ColumnInfo(name = "abandonment") val abandonment: Boolean,
    @ColumnInfo(name = "feature_discoverability") val featureDiscoverability: Double,
    @ColumnInfo(name = "timestamp") val timestamp: Long
)