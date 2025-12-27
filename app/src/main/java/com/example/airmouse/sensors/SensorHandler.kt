package com.example.airmouse.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID
import kotlin.math.abs

class SensorHandler(context: Context) : SensorEventListener {

    // ---------- Unique Session ID ----------
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
            .getReference("sessions/$sessionId/motion")

    // ---------- Motion ----------
    private var dx = 0f
    private var dy = 0f
    private var scroll = 0f

    // ---------- Tuning ----------
    private val sensitivity = 8f
    private val scrollSensitivity = 4f
    private val deadZone = 0.02f

    // ---------- Click ----------
    private val CLICK_THRESHOLD = 7.5f
    private val CLICK_COOLDOWN = 500L
    private var lastClickTime = 0L

    fun start() {
        println("🔥 Firebase Session ID: $sessionId")

        sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME)
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {

        var click = false

        when (event.sensor.type) {

            Sensor.TYPE_GYROSCOPE -> {
                dx = -event.values[1] * sensitivity
                dy = event.values[0] * sensitivity
            }

            Sensor.TYPE_ACCELEROMETER -> {
                val z = event.values[2]
                scroll = z * scrollSensitivity

                if (abs(z) > CLICK_THRESHOLD) {
                    val now = System.currentTimeMillis()
                    if (now - lastClickTime > CLICK_COOLDOWN) {
                        click = true
                        lastClickTime = now
                        println("🖱 CLICK DETECTED!")
                    }
                }
            }
        }

        if (abs(dx) < deadZone) dx = 0f
        if (abs(dy) < deadZone) dy = 0f
        if (abs(scroll) < deadZone) scroll = 0f

        sendToFirebase(click)
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
