package com.kae.engine.audio

import com.kae.engine.math.Vec2
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

class SpatialAudio(private val audioManager: AudioManager) {

    var listenerPosition: Vec2 = Vec2(0f, 0f)

    fun update(sounds: List<Pair<Int, Vec2>>) {
        for ((soundId, worldPosition) in sounds) {
            val (leftVolume, rightVolume) = calculatePanning(
                listenerPosition,
                worldPosition,
                100f
            )
            audioManager.setSoundVolume(soundId, leftVolume, rightVolume)
        }
    }

    fun calculatePanning(
        listenerPos: Vec2,
        soundPos: Vec2,
        maxDistance: Float
    ): Pair<Float, Float> {
        val dx = soundPos.x - listenerPos.x
        val dy = soundPos.y - listenerPos.y
        val distance = sqrt(dx * dx + dy * dy)

        val attenuation = if (distance >= maxDistance) {
            0f
        } else if (distance <= 0f) {
            1f
        } else {
            1f - (distance / maxDistance)
        }

        val angle = atan2(dy, dx)
        val normalizedAngle = (angle + Math.PI.toFloat()) / (2f * Math.PI.toFloat())

        val rightBias = normalizedAngle
        val leftBias = 1f - rightBias

        val leftVolume = attenuation * leftBias
        val rightVolume = attenuation * rightBias

        return Pair(
            leftVolume.coerceIn(0f, 1f),
            rightVolume.coerceIn(0f, 1f)
        )
    }

    fun calculateDistanceAttenuation(
        listenerPos: Vec2,
        soundPos: Vec2,
        maxDistance: Float,
        rolloff: Float
    ): Float {
        val dx = soundPos.x - listenerPos.x
        val dy = soundPos.y - listenerPos.y
        val distance = sqrt(dx * dx + dy * dy)

        if (distance <= 0f) return 1f
        if (distance >= maxDistance) return 0f

        val normalizedDist = distance / maxDistance
        return (1f - normalizedDist).coerceAtLeast(0f).pow(rolloff)
    }

    private fun Float.pow(exponent: Float): Float {
        return this.toDouble().pow(exponent.toDouble()).toFloat()
    }
}
