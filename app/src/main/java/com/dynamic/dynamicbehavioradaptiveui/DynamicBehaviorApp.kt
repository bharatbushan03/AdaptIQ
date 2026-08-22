package com.dynamic.dynamicbehavioradaptiveui

import android.app.Application
import androidx.room.Room
import com.dynamic.dynamicbehavioradaptiveui.models.InteractionEventDatabase

class DynamicBehaviorApp : Application() {
    val interactionDb: InteractionEventDatabase = Room.databaseBuilder(
        applicationContext,
        InteractionEventDatabase::class.java,
        "interaction-events-db"
    ).build()
}