package com.kae.engine.platform

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object FileUtils {

    fun getExternalFilesDir(context: Context): File? {
        return context.getExternalFilesDir(null)
    }

    fun getCacheDir(context: Context): File {
        return context.cacheDir
    }

    fun copyAssetToFile(context: Context, assetPath: String, outputPath: File): Boolean {
        return try {
            val inputStream = context.assets.open(assetPath)
            val outputStream = FileOutputStream(outputPath)
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            outputStream.flush()
            outputStream.close()
            inputStream.close()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    fun readAsset(context: Context, assetPath: String): String? {
        return try {
            context.assets.open(assetPath).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun readAssetBytes(context: Context, assetPath: String): ByteArray? {
        return try {
            context.assets.open(assetPath).use { it.readBytes() }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun fileExists(context: Context, path: String): Boolean {
        val file = if (path.startsWith("/")) {
            File(path)
        } else {
            File(context.filesDir, path)
        }
        return file.exists()
    }

    fun deleteFile(path: String): Boolean {
        val file = File(path)
        return if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }
}
