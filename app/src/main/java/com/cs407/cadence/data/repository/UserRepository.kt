package com.cs407.cadence.data.repository

import android.content.Context
import com.cs407.cadence.data.models.User

/**
 * Handles simple name-based registration and retrieval.
 * Uses SharedPreferences to store the user's name locally.
 */
class UserRepository(private val context: Context) {

    private val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    // create/register — save user's name locally
    fun registerUser(name: String): User {
        sharedPrefs.edit().putString("username", name).apply()
        return User(id = 1, name = name)
    }

    // read/get — retrieve user's name if saved
    fun getUser(): User? {
        val name = sharedPrefs.getString("username", null)
        return if (name != null) User(id = 1, name = name) else null
    }

    // delete/logout — clear saved user data
    fun deleteUser() {
        sharedPrefs.edit().clear().apply()
    }
}
