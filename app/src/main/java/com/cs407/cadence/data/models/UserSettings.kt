package com.cs407.cadence.data.models

data class UserSettings(
    val autoSkipEnabled: Boolean = true,
    val pauseMusicOnStop: Boolean = true,
    val targetPace: Double = 10.0, // minutes per mile
    val preferredGenre: String = ""
)