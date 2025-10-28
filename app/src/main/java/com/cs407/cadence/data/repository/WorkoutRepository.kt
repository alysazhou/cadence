package com.cs407.cadence.data.repository

import com.cs407.cadence.data.models.WorkoutSession

/**
 * WorkoutRepository handles CRUD operations for workout sessions.
 * For now, data is stored locally in memory. this can be replaced later.
 */
class WorkoutRepository {

    // Temporary in-memory storage for workouts
    private val sessions = mutableListOf<WorkoutSession>()

    // CREATE — add a new workout session
    fun createSession(session: WorkoutSession) {
        sessions.add(session)
        println("Workout created: $session")
    }

    // READ — get all saved sessions
    fun getAllSessions(): List<WorkoutSession> {
        return sessions.toList()
    }

    // UPDATE — modify an existing session
    fun updateSession(updatedSession: WorkoutSession) {
        val index = sessions.indexOfFirst { it.id == updatedSession.id }
        if (index != -1) {
            sessions[index] = updatedSession
            println("Workout updated: $updatedSession")
        } else {
            println("Workout not found for update: id=${updatedSession.id}")
        }
    }

    // DELETE — remove all sessions
    fun deleteAllSessions() {
        sessions.clear()
        println("All workouts deleted.")
    }
}
