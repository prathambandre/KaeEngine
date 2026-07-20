package com.kae.engine.audio

import com.kae.engine.scene.Component

data class SoundComponent(
    var soundName: String = "",
    var playOnStart: Boolean = false,
    var loop: Boolean = false,
    var volume: Float = 1f,
    var spatialBlend: Float = 0f,
    var maxDistance: Float = 100f,
    var rolloff: Float = 1f,
    var soundId: Int = -1
) : Component
