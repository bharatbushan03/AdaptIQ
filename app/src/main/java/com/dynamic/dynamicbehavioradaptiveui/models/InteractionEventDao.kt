package com.dynamic.dynamicbehavioradaptiveui.models

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.util.List

@Dao
interface InteractionEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: RoomInteractionEvent)

    @Query("SELECT * FROM interaction_events ORDER BY timestamp DESC")
    suspend fun getAllEvents(): List<RoomInteractionEvent>

    @Query("SELECT * FROM interaction_events WHERE screen = :screen ORDER BY timestamp DESC")
    suspend fun getEventsByScreen(screen: String): List<RoomInteractionEvent>

    @Query("SELECT * FROM interaction_events WHERE workflowId = :workflowId ORDER BY timestamp DESC")
    suspend fun getEventsByWorkflow(workflowId: String): List<RoomInteractionEvent>

    @Query("DELETE FROM interaction_events")
    suspend fun clearAllEvents()
}