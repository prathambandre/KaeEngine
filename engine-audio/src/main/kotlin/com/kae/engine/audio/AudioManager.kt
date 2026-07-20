package com.kae.engine.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool

class AudioManager(private val context: Context) {

    private var soundPool: SoundPool? = null
    private var mediaPlayer: MediaPlayer? = null
    private val loadedSounds = mutableMapOf<String, Int>()
    private var masterVolume: Float = 1f
    private var musicVolume: Float = 1f
    private var sfxVolume: Float = 1f
    private var isInitialized: Boolean = false

    fun initialize(maxStreams: Int = 16) {
        if (isInitialized) return

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(maxStreams)
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool?.setOnLoadCompleteListener { _, _, _ -> }
        isInitialized = true
    }

    fun loadSound(name: String, resId: Int): Boolean {
        val pool = soundPool ?: return false
        return try {
            val soundId = pool.load(context, resId, 1)
            if (soundId > 0) {
                loadedSounds[name] = soundId
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun playSound(
        name: String,
        loop: Boolean = false,
        priority: Int = 0,
        rate: Float = 1f
    ): Int {
        val pool = soundPool ?: return -1
        val soundId = loadedSounds[name] ?: return -1
        val leftVolume = sfxVolume * masterVolume
        val rightVolume = sfxVolume * masterVolume
        return try {
            pool.play(
                soundId,
                leftVolume,
                rightVolume,
                priority,
                if (loop) -1 else 0,
                rate.coerceIn(0.5f, 2.0f)
            )
        } catch (e: Exception) {
            -1
        }
    }

    fun stopSound(soundId: Int) {
        try {
            soundPool?.stop(soundId)
        } catch (e: Exception) {
            // Sound may not be playing
        }
    }

    fun pauseSound(soundId: Int) {
        try {
            soundPool?.pause(soundId)
        } catch (e: Exception) {
            // Sound may not be playing
        }
    }

    fun resumeSound(soundId: Int) {
        try {
            soundPool?.resume(soundId)
        } catch (e: Exception) {
            // Sound may not be playing
        }
    }

    fun setSoundVolume(soundId: Int, leftVolume: Float, rightVolume: Float) {
        val pool = soundPool ?: return
        try {
            pool.setVolume(
                soundId,
                leftVolume.coerceIn(0f, 1f) * masterVolume,
                rightVolume.coerceIn(0f, 1f) * masterVolume
            )
        } catch (e: Exception) {
            // Sound may not be active
        }
    }

    fun playMusic(resId: Int, loop: Boolean = true) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(context, resId)?.apply {
                isLooping = loop
                val volume = musicVolume * masterVolume
                setVolume(volume, volume)
                start()
            }
        } catch (e: Exception) {
            mediaPlayer = null
        }
    }

    fun pauseMusic() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        } catch (e: Exception) {
            // MediaPlayer may be in invalid state
        }
    }

    fun resumeMusic() {
        try {
            if (mediaPlayer?.isPlaying == false) {
                mediaPlayer?.start()
            }
        } catch (e: Exception) {
            // MediaPlayer may be in invalid state
        }
    }

    fun stopMusic() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.prepare()
        } catch (e: Exception) {
            // MediaPlayer may be in invalid state
        }
    }

    fun setMusicVolume(volume: Float) {
        musicVolume = volume.coerceIn(0f, 1f)
        val effectiveVolume = musicVolume * masterVolume
        try {
            mediaPlayer?.setVolume(effectiveVolume, effectiveVolume)
        } catch (e: Exception) {
            // MediaPlayer may not be initialized
        }
    }

    fun setMasterVolume(volume: Float) {
        masterVolume = volume.coerceIn(0f, 1f)
        setMusicVolume(musicVolume)
    }

    fun setSfxVolume(volume: Float) {
        sfxVolume = volume.coerceIn(0f, 1f)
    }

    fun getMasterVolume(): Float = masterVolume

    fun getMusicVolume(): Float = musicVolume

    fun getSfxVolume(): Float = sfxVolume

    fun update() {
        // Spatial audio updates can be triggered here if needed
    }

    fun destroy() {
        try {
            soundPool?.release()
            soundPool = null
            mediaPlayer?.release()
            mediaPlayer = null
            loadedSounds.clear()
            isInitialized = false
        } catch (e: Exception) {
            // Cleanup errors are non-fatal
        }
    }

    fun unloadSound(name: String) {
        val soundId = loadedSounds.remove(name)
        if (soundId != null) {
            try {
                soundPool?.unload(soundId)
            } catch (e: Exception) {
                // Unload errors are non-fatal
            }
        }
    }

    fun isMusicPlaying(): Boolean {
        return try {
            mediaPlayer?.isPlaying == true
        } catch (e: Exception) {
            false
        }
    }
}
