package com.dynamic.dynamicbehavioradaptiveui.models

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [RoomInteractionEvent::class], version = 1, exportSchema = false)
abstract class InteractionEventDatabase : RoomDatabase() {
    abstract fun interactionEventDao(): InteractionEventDao
}