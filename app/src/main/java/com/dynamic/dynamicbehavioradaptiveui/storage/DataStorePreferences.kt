package com.dynamic.dynamicbehavioradaptiveui.storage

import android.content.Context
import android.content.SharedPreferences
import com.dynamic.dynamicbehavioradaptiveui.R

object DataStorePreferences {
    private const val PREFS_NAME = "dynamic_ui_prefs"
    private const val KEY_ADAPTIVE_ENABLED = "adaptive_enabled"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isAdaptiveEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_ADAPTIVE_ENABLED, true)
    }

    fun setAdaptiveEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_ADAPTIVE_ENABLED, enabled).apply()
    }

    fun clearBehavioralHistory(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}