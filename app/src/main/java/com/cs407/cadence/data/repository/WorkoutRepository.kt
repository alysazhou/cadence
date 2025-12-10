package com.cs407.cadence.data.repository

import com.cs407.cadence.data.Activity
import com.cs407.cadence.data.models.RoutePoint
import com.cs407.cadence.data.models.SessionStatus
import com.cs407.cadence.data.models.WorkoutSession
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.tasks.await

/**
 * Handles CRUD operations for workout sessions using Firebase Firestore. This version writes and
 * reads exclusively from:
 *
 * users/{uid}/sessions/{sessionId}
 *
 * so every user has their own workout history.
 */
class WorkoutRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
    }

    /** Creates a brand new workout session. */
    suspend fun startSession(): Result<WorkoutSession> {
        return try {
            val userId = getCurrentUserId()

            val sessionRef =
                    firestore.collection("users").document(userId).collection("sessions").document()

            val session =
                    WorkoutSession(
                            sessionId = sessionRef.id,
                            userId = userId,
                            startTime = Timestamp.now(),
                            status = SessionStatus.ACTIVE.name
                    )

            sessionRef.set(session).await()
            println("Workout created: $session")

            Result.success(session)
        } catch (e: Exception) {
            println("Error creating workout: ${e.message}")
            Result.failure(e)
        }
    }

    /** Updates fields during an active workout session. */
    suspend fun updateSession(
            sessionId: String,
            distance: Double,
            routePoints: List<RoutePoint>,
            songsPlayed: List<String>
    ): Result<Unit> {
        return try {
            val updates =
                    mapOf(
                            "distance" to distance,
                            "routePoints" to routePoints,
                            "songsPlayed" to songsPlayed
                    )

            firestore
                    .collection("users")
                    .document(getCurrentUserId())
                    .collection("sessions")
                    .document(sessionId)
                    .update(updates)
                    .await()

            println("Workout updated: sessionId=$sessionId")
            Result.success(Unit)
        } catch (e: Exception) {
            println("Error updating workout: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun pauseSession(sessionId: String): Result<Unit> {
        return try {
            firestore
                    .collection("users")
                    .document(getCurrentUserId())
                    .collection("sessions")
                    .document(sessionId)
                    .update("status", SessionStatus.PAUSED.name)
                    .await()

            println("Workout paused: $sessionId")
            Result.success(Unit)
        } catch (e: Exception) {
            println("Error pausing workout: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun resumeSession(sessionId: String): Result<Unit> {
        return try {
            firestore
                    .collection("users")
                    .document(getCurrentUserId())
                    .collection("sessions")
                    .document(sessionId)
                    .update("status", SessionStatus.ACTIVE.name)
                    .await()

            println("Workout resumed: $sessionId")
            Result.success(Unit)
        } catch (e: Exception) {
            println("Error resuming workout: ${e.message}")
            Result.failure(e)
        }
    }

    /** Ends the workout and marks it COMPLETED. */
    suspend fun endSession(
            sessionId: String,
            time: Int,
            distance: Double,
            averagePace: Double,
            calories: Int,
            activity: String,
            bpm: Int,
            playedSongsDetails: List<Map<String, Any?>> = emptyList()
    ): Result<Unit> {
        return try {
            val userId = getCurrentUserId()

            val sessionRef =
                    firestore
                            .collection("users")
                            .document(userId)
                            .collection("sessions")
                            .document(sessionId)

            val endTime = Timestamp.now()

            val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val updates =
                    mapOf(
                            "time" to time,
                            "distance" to distance,
                            "averagePace" to averagePace,
                            "calories" to calories,
                            "bpm" to bpm,
                            "activity" to activity,
                            "endTime" to endTime,
                            "status" to SessionStatus.COMPLETED.name,
                            "date" to formattedDate,
                            "playedSongsDetails" to playedSongsDetails
                    )

            sessionRef.update(updates).await()

            println("📦 Session saved to Firestore: $sessionId")
            Result.success(Unit)
        } catch (e: Exception) {
            println("🔥 Firestore error ending session: ${e.message}")
            Result.failure(e)
        }
    }

    /** Gets all COMPLETED sessions for this user. */
    suspend fun getAllSessions(
            limit: Int = 50,
            sortBy: String = "startTime"
    ): Result<List<WorkoutSession>> {
        return try {
            val snapshot =
                    firestore
                            .collection("users")
                            .document(getCurrentUserId())
                            .collection("sessions")
                            .whereEqualTo("status", SessionStatus.COMPLETED.name)
                            .orderBy(sortBy, Query.Direction.DESCENDING)
                            .limit(limit.toLong())
                            .get()
                            .await()

            val sessions = snapshot.documents.mapNotNull { it.toObject(WorkoutSession::class.java) }

            println("Retrieved ${sessions.size} sessions")
            Result.success(sessions)
        } catch (e: Exception) {
            println("Error reading session history: ${e.message}")
            Result.failure(e)
        }
    }

    /** Gets the most recent COMPLETED workout. */
    suspend fun getLastSession(): Result<WorkoutSession?> {
        return try {
            val snapshot =
                    firestore
                            .collection("users")
                            .document(getCurrentUserId())
                            .collection("sessions")
                            .whereEqualTo("status", SessionStatus.COMPLETED.name)
                            .orderBy("endTime", Query.Direction.DESCENDING)
                            .limit(1)
                            .get()
                            .await()

            val session = snapshot.documents.firstOrNull()?.toObject(WorkoutSession::class.java)

            println("Last session retrieved: $session")
            Result.success(session)
        } catch (e: Exception) {
            println("Error getting last session: ${e.message}")
            Result.failure(e)
        }
    }

    /** Fetches a single session by id. */
    suspend fun getSessionById(sessionId: String): Result<WorkoutSession?> {
        return try {
            val snapshot =
                    firestore
                            .collection("users")
                            .document(getCurrentUserId())
                            .collection("sessions")
                            .document(sessionId)
                            .get()
                            .await()

            Result.success(snapshot.toObject(WorkoutSession::class.java))
        } catch (e: Exception) {
            println("Error getting session: ${e.message}")
            Result.failure(e)
        }
    }


    /** Deletes selected sessions. */
    suspend fun deleteSessions(sessionIds: List<String>): Result<Unit> {
        return try {
            val batch = firestore.batch()
            val userId = getCurrentUserId()

            sessionIds.forEach { id ->
                val ref =
                        firestore
                                .collection("users")
                                .document(userId)
                                .collection("sessions")
                                .document(id)
                batch.delete(ref)
            }

            batch.commit().await()
            println("Deleted ${sessionIds.size} sessions")
            Result.success(Unit)
        } catch (e: Exception) {
            println("Error deleting sessions: ${e.message}")
            Result.failure(e)
        }
    }


    /** Deletes a single workout session by its ID. */
    suspend fun deleteWorkoutById(sessionId: String): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            firestore
                .collection("users")
                .document(userId)
                .collection("sessions")
                .document(sessionId)
                .delete()
                .await()

            println("Deleted session: $sessionId")
            Result.success(Unit)
        } catch (e: Exception) {
            println("Error deleting session: ${e.message}")
            Result.failure(e)
        }
    }


    /** Deletes all sessions for this user. */
    suspend fun deleteAllSessions(): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            val snapshot =
                    firestore
                            .collection("users")
                            .document(userId)
                            .collection("sessions")
                            .get()
                            .await()

            val batch = firestore.batch()
            snapshot.documents.forEach { doc -> batch.delete(doc.reference) }

            batch.commit().await()
            println("All sessions deleted")
            Result.success(Unit)
        } catch (e: Exception) {
            println("Error deleting all sessions: ${e.message}")
            Result.failure(e)
        }
    }
}
