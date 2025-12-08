package com.cs407.cadence.data.services

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.sqrt

object MovementDetectionService {
    private const val TAG = "MovementDetection"
    private const val MOVEMENT_THRESHOLD = 1.5
    private const val STATIONARY_THRESHOLD = 0.3
    
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    
    fun initialize(context: Context) {
        if (sensorManager == null) {
            sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            
            if (accelerometer == null) {
                Log.w(TAG, "Accelerometer not available on this device")
            }
        }
    }
    
    fun isAvailable(context: Context): Boolean {
        initialize(context)
        return accelerometer != null
    }
    
    fun startDetection(context: Context): Flow<MovementState> = callbackFlow {
        initialize(context)
        
        if (accelerometer == null) {
            Log.e(TAG, "Accelerometer not available")
            close()
            return@callbackFlow
        }
        
        var lastUpdate = System.currentTimeMillis()
        var lastX = 0f
        var lastY = 0f
        var lastZ = 0f
        
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    val currentTime = System.currentTimeMillis()
                    
                    if (currentTime - lastUpdate > 100) { // check every 100ms
                        val x = it.values[0]
                        val y = it.values[1]
                        val z = it.values[2]
                        
                        // Calculate acceleration magnitude 
                        val deltaX = x - lastX
                        val deltaY = y - lastY
                        val deltaZ = z - lastZ
                        
                        val acceleration = sqrt(
                            (deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ).toDouble()
                        )
                        
                        val isMoving = when {
                            acceleration > MOVEMENT_THRESHOLD -> true
                            acceleration < STATIONARY_THRESHOLD -> false
                            else -> null 
                        }
                        
                        isMoving?.let { moving ->
                            trySend(MovementState(moving, acceleration))
                        }
                        
                        lastX = x
                        lastY = y
                        lastZ = z
                        lastUpdate = currentTime
                    }
                }
            }
            
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                Log.d(TAG, "Accelerometer accuracy changed: $accuracy")
            }
        }
        
        sensorManager?.registerListener(
            listener,
            accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        
        Log.d(TAG, "Movement detection started")
        
        awaitClose {
            Log.d(TAG, "Stopping movement detection")
            sensorManager?.unregisterListener(listener)
        }
    }
}

data class MovementState(val isMoving: Boolean, val acceleration: Double)
