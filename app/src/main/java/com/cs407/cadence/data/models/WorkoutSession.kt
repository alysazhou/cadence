package com.cs407.cadence.data.models

data class WorkoutSession(
    val id: Int,
    val activity: String,
    val date: String,
    val bpm: Int,
    val distance: Double,
    val time: Int,
    val calories: Int
)