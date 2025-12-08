package com.cs407.cadence.data.models

import com.google.firebase.Timestamp

data class WorkoutSession(
        // auto populated by firestore
        val id: String? = null,
        val sessionId: String = "",
        val userId: String = "",
        val activity: String = "Running",
        val startTime: Timestamp = Timestamp.now(),
        val endTime: Timestamp? = null,
        val bpm: Int = 0,
        val distance: Double = 0.0,
        val time: Int = 0,
        val calories: Int = 0,
        val averagePace: Double = 0.0,
        val routePoints: List<RoutePoint> = emptyList(),
        val songsPlayed: List<String> = emptyList(),
        val playedSongsDetails: List<Map<String, Any?>> = emptyList(),
        val status: String = SessionStatus.ACTIVE.name,
        val date: String = ""
) {
    fun toStatusEnum(): SessionStatus = SessionStatus.valueOf(status)
}
