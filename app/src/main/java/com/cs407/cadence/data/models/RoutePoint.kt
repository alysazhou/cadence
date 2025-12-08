package com.cs407.cadence.data.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class RoutePoint(
    val location: GeoPoint = GeoPoint(0.0, 0.0),
    val timestamp: Timestamp = Timestamp.now(),
    val pace: Double = 0.0
)