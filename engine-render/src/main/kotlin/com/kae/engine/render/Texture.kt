package com.kae.engine.render

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.opengl.GLES30
import android.opengl.GLUtils
import java.io.File

class Texture {

    var textureId: Int = 0
        private set

    var width: Int = 0
        private set

    var height: Int = 0
        private set

    private var target: Int = GLES30.GL_TEXTURE_2D

    constructor(context: Context, resId: Int) {
        val options = BitmapFactory.Options().apply {
            inScaled = false
        }
        val bitmap = BitmapFactory.decodeResource(context.resources, resId, options)
            ?: throw RuntimeException("Failed to decode texture resource $resId")
        loadFromBitmap(bitmap)
        bitmap.recycle()
    }

    constructor(bitmap: Bitmap) {
        loadFromBitmap(bitmap)
    }

    private fun loadFromBitmap(bitmap: Bitmap) {
        width = bitmap.width
        height = bitmap.height

        val ids = IntArray(1)
        GLES30.glGenTextures(1, ids, 0)
        textureId = ids[0]

        GLES30.glBindTexture(target, textureId)

        GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR)
        GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)

        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
        GLES30.glGenerateMipmap(target)

        GLES30.glBindTexture(target, 0)
    }

    fun bind(unit: Int = 0) {
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0 + unit)
        GLES30.glBindTexture(target, textureId)
    }

    fun unbind() {
        GLES30.glBindTexture(target, 0)
    }

    fun dispose() {
        if (textureId != 0) {
            GLES30.glDeleteTextures(1, intArrayOf(textureId), 0)
            textureId = 0
        }
    }

    companion object {
        fun loadFromFile(path: String): Texture {
            val file = File(path)
            if (!file.exists()) {
                throw RuntimeException("Texture file not found: $path")
            }
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                ?: throw RuntimeException("Failed to decode texture file: $path")
            val texture = Texture(bitmap)
            bitmap.recycle()
            return texture
        }
    }
}
