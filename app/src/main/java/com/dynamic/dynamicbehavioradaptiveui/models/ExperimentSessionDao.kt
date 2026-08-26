package com.dynamic.dynamicbehavioradaptiveui.models

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExperimentSessionDao {
    @Insert
    suspend fun insertSession(session: ExperimentSession)

    @Insert
    suspend fun insertSessions(sessions: List<ExperimentSession>)

    @Query("SELECT * FROM experiment_sessions WHERE session_id = :sessionId")
    fun getSession(sessionId: String): ExperimentSession?

    @Query("SELECT * FROM experiment_sessions")
    fun getAllSessions(): Flow<List<ExperimentSession>>

    @Query("DELETE FROM experiment_sessions")
    suspend fun clearAllSessions()
}