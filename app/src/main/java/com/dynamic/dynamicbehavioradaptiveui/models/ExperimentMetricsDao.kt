package com.dynamic.dynamicbehavioradaptiveui.models

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExperimentMetricsDao {
    @Insert
    suspend fun insertMetrics(metrics: ExperimentMetrics)

    @Insert
    suspend fun insertMetricsBatch(metrics: List<ExperimentMetrics>)

    @Query("SELECT * FROM experiment_metrics WHERE session_id = :sessionId")
    fun getMetricsForSession(sessionId: String): Flow<List<ExperimentMetrics>>

    @Query("SELECT * FROM experiment_metrics WHERE mode = :mode")
    fun getMetricsByMode(mode: ExperimentMode): Flow<List<ExperimentMetrics>>

    @Query("SELECT * FROM experiment_metrics")
    fun getAllMetrics(): Flow<List<ExperimentMetrics>>

    @Query("SELECT DISTINCT target as feature FROM experiment_metrics WHERE target != '' AND mode = :mode")
    fun getMostUsedFeatures(mode: ExperimentMode): Flow<List<String>>

    @Query("SELECT previousScreen || ' -> ' || screen AS navigation_path FROM interaction_events WHERE previousScreen != '' AND action LIKE '%navigation%'")
    fun getNavigationPaths(): Flow<List<String>>

    @Query("SELECT COUNT(*) as backtrackCount FROM interaction_events WHERE action LIKE '%back_navigation%'")
    fun getBacktrackingCount(mode: ExperimentMode): Flow<List<ExperimentMetrics>>

    @Query("SELECT COUNT(*) as errorCount FROM interaction_events WHERE success = 0 AND mode = :mode")
    fun getErrorCountByMode(mode: ExperimentMode): Flow<List<ExperimentMetrics>>

    @Query("DELETE FROM experiment_metrics")
    suspend fun clearAllMetrics()

    @Query("DELETE FROM interaction_events")
    suspend fun clearAllEvents()
}