package com.cs407.cadence.data.repository

import android.content.Context
import com.cs407.cadence.data.models.User
import com.cs407.cadence.data.models.UserSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
    }

    suspend fun saveUser(user: User): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(user.userId)
                .set(user)
                .await()
            println("User saved: ${user.name}")
            Result.success(Unit)
        } catch (e: Exception) {
            println("Error saving user: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getUser(userId: String = getCurrentUserId()): Result<User?> {
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            val user = snapshot.toObject(User::class.java)
            println("User retrieved: ${user?.name}")
            Result.success(user)
        } catch (e: Exception) {
            println("Error getting user: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateSettings(settings: UserSettings): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(getCurrentUserId())
                .update("settings", settings)
                .await()
            println("Settings updated: $settings")
            Result.success(Unit)
        } catch (e: Exception) {
            println("Error updating settings: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateUserName(name: String): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(getCurrentUserId())
                .update("name", name)
                .await()
            println("User name updated: $name")
            Result.success(Unit)
        } catch (e: Exception) {
            println("Error updating name: ${e.message}")
            Result.failure(e)
        }
    }
}