package com.dynamic.dynamicbehavioradaptiveui.models

import android.app.Application
import androidx.room.Room
import com.dynamic.dynamicbehavioradaptiveui.models.InteractionEventDatabase

object RoomDatabaseHolder {
    @Volatile
    private var instance: InteractionEventDatabase? = null

    fun getDatabase(app: Application): InteractionEventDatabase {
        if (instance == null) {
            synchronized(this) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        app.applicationContext,
                        InteractionEventDatabase::class.java,
                        "interaction-events-db"
                    ).build()
                }
            }
        }
        return instance!!
    }
}