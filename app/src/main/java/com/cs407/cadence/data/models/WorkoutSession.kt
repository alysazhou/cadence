package com.cs407.cadence.data.models

data class WorkoutSession(
    val id: Int, // to tell workouts apart
    val date: String,
    val durationMinutes: Int,
    val distanceKm: Double,
    val pace: Double
)
