package com.example.airmouse.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID
import kotlin.math.abs

class SensorHandler(context: Context) : SensorEventListener {

    // ---------- Session ----------
    private val sessionId: String = UUID.randomUUID().toString()

    // ---------- Sensors ----------
    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val gyro =
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    // REVERTED: Changed back to standard accelerometer for scroll
    private val accel =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // ---------- Firebase ----------
    private val dbRef =
        FirebaseDatabase.getInstance()
            .getReference("sessions/$sessionId/motion")

    // ---------- Motion ----------
    private var dx = 0f
    private var dy = 0f
    private var scroll = 0f

    // ---------- Tuning ----------
    private val sensitivity = 8f
    private val scrollSensitivity = 4f // Reverted to original value
    private val deadZone = 0.02f // Reverted to original value

    // ---------- Click ----------
    private val CLICK_COOLDOWN = 500L
    private var lastClickDetectionTime = 0L
    private var lastClickActivationTime = 0L

    // NEW: Public method to be called from MainActivity when the screen is tapped
    fun triggerClick() {
        val now = System.currentTimeMillis()
        if (now - lastClickDetectionTime > CLICK_COOLDOWN) {
            lastClickDetectionTime = now
            lastClickActivationTime = now
            Log.d("CLICK", "CLICK TRIGGERED from UI tap")
        }
    }

    fun start() {
        Log.d("SensorHandler", "Session ID: $sessionId")

        sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME)
        // REVERTED: Listening to the standard accelerometer again
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME)
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {

        when (event.sensor.type) {

            // -------- Cursor movement --------
            Sensor.TYPE_GYROSCOPE -> {
                dx = -event.values[1] * sensitivity
                dy = event.values[0] * sensitivity
            }

            // -------- Scroll (Reverted to original logic) --------
            Sensor.TYPE_ACCELEROMETER -> {
                // Scroll is based on Z-axis tilt (forward/backward)
                val z = event.values[2]
                scroll = z * scrollSensitivity

                // REMOVED: Physical click detection logic is no longer here.
            }
        }

        // Dead zone
        if (abs(dx) < deadZone) dx = 0f
        if (abs(dy) < deadZone) dy = 0f
        if (abs(scroll) < deadZone) scroll = 0f

        // -------- Click state logic (This part remains to manage the 1-second signal) --------
        val now = System.currentTimeMillis()
        var clickIsActive = false

        if (lastClickActivationTime != 0L) {
            if ((now - lastClickActivationTime) < 750L) {
                clickIsActive = true
            } else {
                // Reset after 1 second has passed
                lastClickActivationTime = 0L
            }
        }

        sendToFirebase(clickIsActive)
    }

    private fun sendToFirebase(click: Boolean) {
        val data = mapOf(
            "dx" to dx,
            "dy" to dy,
            "scroll" to scroll,
            "click" to click,
            "timestamp" to System.currentTimeMillis()
        )
        dbRef.setValue(data)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
