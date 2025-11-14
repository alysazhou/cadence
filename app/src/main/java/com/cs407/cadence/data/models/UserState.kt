package com.cs407.cadence.data.models

data class UserState(
    val id: Int = 0, // Room database ID
    val name: String = "", // Display name
    val uid: String = "" // Firebase UID
)