package com.kae.engine.audio

import android.content.Context
import android.media.MediaPlayer

class MusicPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null

    var isPlaying: Boolean = false
        private set

    var volume: Float = 1f

    fun load(resId: Int) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(context, resId)
            mediaPlayer?.setVolume(volume, volume)
        } catch (e: Exception) {
            mediaPlayer = null
            isPlaying = false
        }
    }

    fun play(loop: Boolean = true) {
        val player = mediaPlayer ?: return
        try {
            player.isLooping = loop
            player.setVolume(volume, volume)
            player.start()
            isPlaying = true
        } catch (e: Exception) {
            isPlaying = false
        }
    }

    fun pause() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                isPlaying = false
            }
        } catch (e: Exception) {
            isPlaying = false
        }
    }

    fun resume() {
        try {
            if (mediaPlayer?.isPlaying == false) {
                mediaPlayer?.start()
                isPlaying = true
            }
        } catch (e: Exception) {
            // MediaPlayer may be in invalid state
        }
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.prepare()
            isPlaying = false
        } catch (e: Exception) {
            isPlaying = false
        }
    }

    fun seekTo(positionMs: Int) {
        try {
            mediaPlayer?.seekTo(positionMs)
        } catch (e: Exception) {
            // MediaPlayer may not be initialized
        }
    }

    val durationMs: Int
        get() = try {
            mediaPlayer?.duration ?: 0
        } catch (e: Exception) {
            0
        }

    val positionMs: Int
        get() = try {
            mediaPlayer?.currentPosition ?: 0
        } catch (e: Exception) {
            0
        }

    fun destroy() {
        try {
            mediaPlayer?.release()
            mediaPlayer = null
            isPlaying = false
        } catch (e: Exception) {
            // Cleanup errors are non-fatal
        }
    }
}
