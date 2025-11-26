package com.cs407.cadence.data.repository

import com.cs407.cadence.data.models.WorkoutSession
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseWorkoutRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun createSession(uid: String, session: WorkoutSession) {
        db.collection("users")
            .document(uid)
            .collection("workouts")
            .add(session)
            .await()
    }

    suspend fun getAllSessions(uid: String): List<WorkoutSession> {
        val snapshot = db.collection("users")
            .document(uid)
            .collection("workouts")
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(WorkoutSession::class.java)
        }
    }
}
