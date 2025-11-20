package com.cs407.cadence.location

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.Flow

class LocationService(context: Context) {

    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun locationUpdates(): Flow<LocationResult> = callbackFlow {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 2000L  // every 2 seconds
        ).setMinUpdateDistanceMeters(1f).build()   // only record if moved ≥ 1 meter

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                trySend(result)
            }
        }

        fusedLocationClient.requestLocationUpdates(request, callback, null)

        awaitClose {
            fusedLocationClient.removeLocationUpdates(callback)
        }
    }
}
