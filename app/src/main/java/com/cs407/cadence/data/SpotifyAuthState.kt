package com.cs407.cadence.data

import android.content.Context
import android.content.SharedPreferences

object SpotifyAuthState {
    private const val PREFS_NAME = "spotify_auth"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_IS_AUTHENTICATED = "is_authenticated"
    private const val KEY_TOKEN_EXPIRY = "token_expiry"
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    fun saveAccessToken(context: Context, token: String, expiresInSeconds: Long = 3600) {
        val expiryTime = System.currentTimeMillis() + (expiresInSeconds * 1000)
        getPrefs(context).edit().apply {
            putString(KEY_ACCESS_TOKEN, token)
            putBoolean(KEY_IS_AUTHENTICATED, true)
            putLong(KEY_TOKEN_EXPIRY, expiryTime)
            apply()
        }
    }
    
    fun getAccessToken(context: Context): String? {
        return getPrefs(context).getString(KEY_ACCESS_TOKEN, null)
    }
    
    fun isAuthenticated(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_IS_AUTHENTICATED, false)
    }
    
    fun isTokenValid(context: Context): Boolean {
        if (!isAuthenticated(context)) return false
        val expiryTime = getPrefs(context).getLong(KEY_TOKEN_EXPIRY, 0)
        return System.currentTimeMillis() < expiryTime
    }
    
    fun clearAuth(context: Context) {
        getPrefs(context).edit().apply {
            remove(KEY_ACCESS_TOKEN)
            putBoolean(KEY_IS_AUTHENTICATED, false)
            apply()
        }
    }
}
