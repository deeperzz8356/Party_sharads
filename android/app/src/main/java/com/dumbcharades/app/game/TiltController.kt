package com.dumbcharades.app.game

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.math.abs

enum class TiltGesture { CORRECT, SKIP }

class TiltGate(private val threshold: Float, private val neutralBand: Float = 18f, private val debounceMs: Long = 650L) {
    private var neutralPitch: Float? = null
    private var armed = true
    private var lastEvent = 0L

    fun calibrate(pitch: Float) { neutralPitch = pitch; armed = true }

    fun accept(pitch: Float, now: Long): TiltGesture? {
        val neutral = neutralPitch ?: return null.also { calibrate(pitch) }
        val delta = normalizeDegrees(pitch - neutral)
        if (abs(delta) <= neutralBand) armed = true
        if (!armed || now - lastEvent < debounceMs) return null
        val gesture = when {
            delta <= -threshold -> TiltGesture.CORRECT
            delta >= threshold -> TiltGesture.SKIP
            else -> null
        }
        if (gesture != null) { armed = false; lastEvent = now }
        return gesture
    }

    private fun normalizeDegrees(value: Float): Float = ((value + 180f) % 360f + 360f) % 360f - 180f
}

class TiltController @Inject constructor(@ApplicationContext context: Context) : SensorEventListener {
    private val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sensor = manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private var listener: ((TiltGesture) -> Unit)? = null
    private var gate = TiltGate(50f)
    private val rotation = FloatArray(9)
    private val orientation = FloatArray(3)
    val available: Boolean get() = sensor != null

    fun start(threshold: Float, callback: (TiltGesture) -> Unit) {
        gate = TiltGate(threshold)
        listener = callback
        sensor?.let { manager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
    }

    fun stop() { manager.unregisterListener(this); listener = null }

    fun recalibrate() { orientation.fill(0f); gate = TiltGate(50f) }

    override fun onSensorChanged(event: SensorEvent) {
        SensorManager.getRotationMatrixFromVector(rotation, event.values)
        SensorManager.getOrientation(rotation, orientation)
        val pitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
        gate.accept(pitch, System.currentTimeMillis())?.let { listener?.invoke(it) }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}

