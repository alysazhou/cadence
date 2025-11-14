package com.cs407.cadence.data.models

data class WorkoutSession(
    //firestore requires every field in its data class to have a default value
    val id: Int = 0,
    val activity: String = "",
    val date: String = "",
    val bpm: Int = 0,
    val distance: Double = 0.0,
    val time: Int = 0,
    val calories: Int = 0,
)