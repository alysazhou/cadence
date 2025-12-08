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

object StepCounterService {
    private const val TAG = "StepCounterService"
    private const val AVERAGE_STRIDE_LENGTH_METERS = 0.762 // avg stride length

    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor? = null
    private var initialSteps: Int? = null

    fun initialize(context: Context) {
        if (sensorManager == null) {
            sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

            if (stepSensor == null) {
                Log.w(TAG, "Step counter sensor not available on this device")
            }
        }
    }

    fun isAvailable(context: Context): Boolean {
        initialize(context)
        return stepSensor != null
    }

    fun startCounting(context: Context): Flow<StepUpdate> = callbackFlow {
        initialize(context)

        if (stepSensor == null) {
            Log.e(TAG, "Step counter not available")
            close()
            return@callbackFlow
        }

        initialSteps = null
        Log.d(TAG, "Step counter sensor found: ${stepSensor?.name}")

        val listener =
                object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent?) {
                        event?.let {
                            val totalSteps = it.values[0].toInt()
                            Log.d(TAG, "Step sensor update: $totalSteps total steps")

                            if (initialSteps == null) {
                                initialSteps = totalSteps
                                Log.d(TAG, "Initial step count set: $totalSteps")
                            }

                            val workoutSteps = totalSteps - (initialSteps ?: totalSteps)
                            val distanceMeters = workoutSteps * AVERAGE_STRIDE_LENGTH_METERS

                            Log.d(TAG, "Workout steps: $workoutSteps, Distance: ${distanceMeters}m")
                            trySend(StepUpdate(workoutSteps, distanceMeters))
                        }
                    }

                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                        Log.d(TAG, "Sensor accuracy changed: $accuracy")
                    }
                }

        val registered =
                sensorManager?.registerListener(listener, stepSensor, SensorManager.SENSOR_DELAY_UI)
        Log.d(TAG, "Step counting started, listener registered: $registered")

        awaitClose {
            Log.d(TAG, "Stopping step counting")
            sensorManager?.unregisterListener(listener)
            initialSteps = null
        }
    }

    fun resetSteps() {
        initialSteps = null
    }
}

data class StepUpdate(val steps: Int, val distanceMeters: Double)
