package com.cs407.cadence.data.models

import com.google.firebase.Timestamp

data class User(
    val userId: String = "",
    val name: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val settings: UserSettings = UserSettings()
)