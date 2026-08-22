package com.dynamic.dynamicbehavioradaptiveui.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "interaction_events")
data class RoomInteractionEvent(
    @PrimaryKey(val autoGenerate = true)
    val eventId: String = "",
    val timestamp: Long = 0L,
    val sessionId: String = "",
    val screen: String = "",
    val action: String = "",
    val target: String = "",
    val previousScreen: String = "",
    val duration: Long = 0L,
    val workflowId: String = "",
    val success: Int = 1,
    val metadata: String = ""
)