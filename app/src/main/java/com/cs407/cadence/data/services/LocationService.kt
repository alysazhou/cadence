package com.cs407.cadence.data.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlin.math.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

object LocationService {
    private const val TAG = "LocationService"
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var lastLocation: Location? = null
    private var totalDistance: Double = 0.0

    fun initialize(context: Context) {
        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        }
    }

    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun startTracking(context: Context): Flow<LocationUpdate> = callbackFlow {
        if (!hasLocationPermission(context)) {
            Log.e(TAG, "Location permission not granted")
            close()
            return@callbackFlow
        }

        initialize(context)
        lastLocation = null
        totalDistance = 0.0

        val locationRequest =
                LocationRequest.Builder(
                                Priority.PRIORITY_HIGH_ACCURACY,
                                5000L // updates every 5 seconds
                        )
                        .apply {
                            setMinUpdateIntervalMillis(2000L) // every 2 seconds
                            setWaitForAccurateLocation(true)
                        }
                        .build()

        val locationCallback =
                object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        result.lastLocation?.let { location ->
                            // require better accuracy (20m) to reduce GPS drift
                            if (location.accuracy > 20) {
                                Log.d(
                                        TAG,
                                        "Ignoring inaccurate location (accuracy: ${location.accuracy}m)"
                                )
                                return
                            }

                            lastLocation?.let { last ->
                                val distance =
                                        calculateDistance(
                                                last.latitude,
                                                last.longitude,
                                                location.latitude,
                                                location.longitude
                                        )

                                // filter out GPS drift: require minimum 3m movement
                                if (distance < 3.0) {
                                    Log.d(
                                            TAG,
                                            "Ignoring GPS drift: ${distance}m (below 3m threshold)"
                                    )
                                    lastLocation = location
                                    trySend(LocationUpdate(location, totalDistance))
                                    return
                                }

                                // filter out unrealistic jumps
                                val timeDiff = (location.time - last.time) / 1000.0
                                if (timeDiff > 0) {
                                    val speed = distance / timeDiff
                                    if (speed < 15) { // max running speed ~15 m/s
                                        totalDistance += distance
                                        Log.d(
                                                TAG,
                                                "Distance update: +${distance}m, total: ${totalDistance}m (speed: ${speed} m/s)"
                                        )
                                    } else {
                                        Log.d(
                                                TAG,
                                                "Ignoring erratic GPS jump (speed: ${speed} m/s)"
                                        )
                                    }
                                }
                            }

                            lastLocation = location
                            trySend(LocationUpdate(location, totalDistance))
                        }
                    }
                }

        try {
            fusedLocationClient?.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
            )
            Log.d(TAG, "Location tracking started")
        } catch (e: SecurityException) {
            Log.e(TAG, "Location permission error: ${e.message}")
            close(e)
        }

        awaitClose {
            Log.d(TAG, "Stopping location tracking")
            fusedLocationClient?.removeLocationUpdates(locationCallback)
        }
    }

    fun resetDistance() {
        totalDistance = 0.0
        lastLocation = null
    }

    /** calculates distance */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371000.0 // meters

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a =
                sin(dLat / 2) * sin(dLat / 2) +
                        cos(Math.toRadians(lat1)) *
                                cos(Math.toRadians(lat2)) *
                                sin(dLon / 2) *
                                sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }

    /** calculate calories based on activity type */
    fun calculateCalories(
            activityType: String,
            durationMinutes: Int,
            avgSpeedKmh: Double,
            weightKg: Double = 70.0
    ): Int {
        val hours = durationMinutes / 60.0

        val met =
                when (activityType.lowercase()) {
                    "walking" ->
                            when {
                                avgSpeedKmh < 3.2 -> 2.5
                                avgSpeedKmh < 4.8 -> 3.5
                                avgSpeedKmh < 6.4 -> 4.3
                                else -> 5.0
                            }
                    "jogging" ->
                            when {
                                avgSpeedKmh < 8.0 -> 7.0
                                avgSpeedKmh < 9.7 -> 8.3
                                else -> 9.0
                            }
                    "running" ->
                            when {
                                avgSpeedKmh < 9.7 -> 9.0
                                avgSpeedKmh < 11.3 -> 10.5
                                avgSpeedKmh < 12.9 -> 11.5
                                avgSpeedKmh < 14.5 -> 12.5
                                else -> 13.5
                            }
                    else -> 8.0
                }

        return (met * weightKg * hours).toInt()
    }
}

data class LocationUpdate(val location: Location, val totalDistanceMeters: Double)
