package com.dynamic.dynamicbehavioradaptiveui.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "experiment_sessions")
data class ExperimentSession(
    @PrimaryKey @ColumnInfo(name = "session_id") val sessionId: String,
    @ColumnInfo(name = "mode") val mode: ExperimentMode,
    @ColumnInfo(name = "start_time") val startTime: Long,
    @ColumnInfo(name = "end_time") var endTime: Long,
    @ColumnInfo(name = "baseline_task_count") val baselineTaskCount: Int,
    @ColumnInfo(name = "adaptive_task_count") val adaptiveTaskCount: Int,
    @ColumnInfo(name = "status") val status: ExperimentStatus,
    @ColumnInfo(name = "notes") val notes: String = ""
)

enum class ExperimentMode {
    BASELINE,
    ADAPTIVE
}

enum class ExperimentStatus {
    ACTIVE,
    COMPLETED,
    ABANDONED
}