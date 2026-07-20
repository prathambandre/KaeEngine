package com.kae.engine.assets

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.Executors

class AssetLoader(private val context: Context) {

    private val executor = Executors.newFixedThreadPool(2)

    fun loadAsync(assetPath: String, type: AssetType, onComplete: (Any?) -> Unit) {
        executor.submit {
            try {
                val result = when (type) {
                    AssetType.TEXTURE -> loadTexture(assetPath)
                    AssetType.AUDIO -> loadBinary(assetPath)
                    AssetType.JSON -> loadText(assetPath)
                    AssetType.SHADER -> loadText(assetPath)
                }
                onComplete(result)
            } catch (e: Exception) {
                onComplete(null)
            }
        }
    }

    fun loadTexture(path: String): Bitmap? {
        return try {
            val inputStream = try {
                context.assets.open(path)
            } catch (e: Exception) {
                val file = File(path)
                if (file.exists()) FileInputStream(file) else null
            }
            inputStream?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            null
        }
    }

    fun loadText(path: String): String? {
        return try {
            val inputStream = try {
                context.assets.open(path)
            } catch (e: Exception) {
                val file = File(path)
                if (file.exists()) FileInputStream(file) else null
            }
            inputStream?.use { stream ->
                val buffer = ByteArrayOutputStream()
                val data = ByteArray(4096)
                var bytesRead: Int
                while (stream.read(data).also { bytesRead = it } != -1) {
                    buffer.write(data, 0, bytesRead)
                }
                buffer.toString("UTF-8")
            }
        } catch (e: Exception) {
            null
        }
    }

    fun loadBinary(path: String): ByteArray? {
        return try {
            val inputStream = try {
                context.assets.open(path)
            } catch (e: Exception) {
                val file = File(path)
                if (file.exists()) FileInputStream(file) else null
            }
            inputStream?.use { stream ->
                val buffer = ByteArrayOutputStream()
                val data = ByteArray(4096)
                var bytesRead: Int
                while (stream.read(data).also { bytesRead = it } != -1) {
                    buffer.write(data, 0, bytesRead)
                }
                buffer.toByteArray()
            }
        } catch (e: Exception) {
            null
        }
    }

    fun shutdown() {
        executor.shutdownNow()
    }
}
