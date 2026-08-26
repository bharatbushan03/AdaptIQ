package com.dynamic.dynamicbehavioradaptiveui.models

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [RoomInteractionEvent::class, ExperimentSession::class, ExperimentMetrics::class], version = 2, exportSchema = false)
abstract class InteractionEventDatabase : RoomDatabase() {
    abstract fun interactionEventDao(): InteractionEventDao
    abstract fun experimentSessionDao(): ExperimentSessionDao
    abstract fun experimentMetricsDao(): ExperimentMetricsDao