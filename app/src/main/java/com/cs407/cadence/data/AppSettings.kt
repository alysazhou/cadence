package com.cs407.cadence.data

import android.content.Context
import android.content.SharedPreferences

object AppSettings {
    private const val PREFS_NAME = "app_settings"
    private const val KEY_AUTO_STOP_ENABLED = "auto_stop_enabled"
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    fun setAutoStopEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().apply {
            putBoolean(KEY_AUTO_STOP_ENABLED, enabled)
            apply()
        }
    }
    
    fun isAutoStopEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_AUTO_STOP_ENABLED, false)
    }
}
