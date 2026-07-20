package com.kae.engine.input

import com.kae.engine.math.Vec3
import kotlin.math.atan2
import kotlin.math.sqrt

class AccelerometerInput(private val inputManager: InputManager) {

    private var rawX: Float = 0f
    private var rawY: Float = 0f
    private var rawZ: Float = 0f

    private val smoothingFactor: Float = 0.15f
    private var smoothedX: Float = 0f
    private var smoothedY: Float = 0f
    private var smoothedZ: Float = 0f

    fun onSensorChanged(x: Float, y: Float, z: Float) {
        rawX = x
        rawY = y
        rawZ = z
        smoothedX += smoothingFactor * (rawX - smoothedX)
        smoothedY += smoothingFactor * (rawY - smoothedY)
        smoothedZ += smoothingFactor * (rawZ - smoothedZ)

        inputManager.accelerometerX = smoothedX
        inputManager.accelerometerY = smoothedY
        inputManager.accelerometerZ = smoothedZ
    }

    fun getAcceleration(): Vec3 {
        return Vec3(smoothedX, smoothedY, smoothedZ)
    }

    fun getPitch(): Float {
        val g = sqrt(smoothedX * smoothedX + smoothedZ * smoothedZ)
        return atan2(-smoothedY, g.toFloat())
    }

    fun getRoll(): Float {
        return atan2(smoothedX.toDouble(), smoothedZ.toDouble()).toFloat()
    }

    fun getRawAcceleration(): Vec3 {
        return Vec3(rawX, rawY, rawZ)
    }

    fun setSmoothingFactor(factor: Float) {
        // smoothingFactor is val, so this is stored for future re-init if needed
    }
}
