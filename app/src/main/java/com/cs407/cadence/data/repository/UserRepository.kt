package com.cs407.cadence.data.repository

import android.content.Context
import com.cs407.cadence.data.models.User

/**
 * handles user registration and retrieval, stores username locally
 */
class UserRepository(private val context: Context) {

    private val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    // REGISTER USER
    fun registerUser(name: String): User {
        sharedPrefs.edit().putString("username", name).apply()
        return User(id = 1, name = name)
    }

    // GET USERNAME
    fun getUser(): User? {
        val name = sharedPrefs.getString("username", null)
        return if (name != null) User(id = 1, name = name) else null
    }

    // DELETE
    fun deleteUser() {
        sharedPrefs.edit().clear().apply()
    }
}
