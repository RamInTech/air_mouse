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

    private val accel =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // ---------- Firebase ----------
    private val dbRef =
        FirebaseDatabase.getInstance()
            .getReference("sessions/$sessionId")

    private val motionDbRef = dbRef.child("motion")

    // ---------- Motion ----------
    private var dx = 0f
    private var dy = 0f
    private var scroll = 0f

    // ---------- Tuning ----------
    private val sensitivity = 8f
    private val scrollSensitivity = 4f
    private val deadZone = 0.02f

    // ---------- Click ----------
    private val CLICK_COOLDOWN = 500L
    private var lastClickDetectionTime = 0L
    private var lastClickActivationTime = 0L

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

        // Sets the session to active
        dbRef.child("active").setValue(true)

        sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME)
    }

    fun stop() {
        sensorManager.unregisterListener(this)

        // Sets the session to inactive
        dbRef.child("active").setValue(false)
    }

    override fun onSensorChanged(event: SensorEvent) {

        when (event.sensor.type) {

            Sensor.TYPE_GYROSCOPE -> {
                dx = -event.values[1] * sensitivity
                dy = event.values[0] * sensitivity
            }

            Sensor.TYPE_ACCELEROMETER -> {
                val z = event.values[2]
                scroll = z * scrollSensitivity
            }
        }

        if (abs(dx) < deadZone) dx = 0f
        if (abs(dy) < deadZone) dy = 0f
        if (abs(scroll) < deadZone) scroll = 0f

        val now = System.currentTimeMillis()
        var clickIsActive = false

        if (lastClickActivationTime != 0L) {
            if ((now - lastClickActivationTime) < 750L) {
                clickIsActive = true
            } else {
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
        motionDbRef.setValue(data)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
