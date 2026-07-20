package com.kae.engine.platform

import android.app.ActivityManager
import android.content.Context
import android.opengl.GLES30
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager

object DeviceCapabilities {

    fun getGLVersion(context: Context): String {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val configurationInfo = activityManager.deviceConfigurationInfo
            configurationInfo.glEsVersion
        } catch (e: Exception) {
            "0.0.0"
        }
    }

    fun hasOpenGLES30(context: Context): Boolean {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val configurationInfo = activityManager.deviceConfigurationInfo
            configurationInfo.reqGlEsVersion >= 0x30000
        } catch (e: Exception) {
            false
        }
    }

    fun hasOpenGLES32(context: Context): Boolean {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val configurationInfo = activityManager.deviceConfigurationInfo
            configurationInfo.reqGlEsVersion >= 0x30002
        } catch (e: Exception) {
            false
        }
    }

    fun getAvailableMemory(context: Context): Long {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.availMem
    }

    fun getDisplayMetrics(context: Context): DisplayMetrics {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        display.getRealMetrics(metrics)
        return metrics
    }

    fun getDeviceModel(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL}"
    }

    fun getOSVersion(): String {
        return "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
    }

    fun getMaxTextureSize(): Int {
        val maxSize = IntArray(1)
        GLES30.glGetIntegerv(GLES30.GL_MAX_TEXTURE_SIZE, maxSize, 0)
        return maxSize[0]
    }
}
