package com.kae.engine.assets

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.ConcurrentLinkedQueue

class AssetManager(private val context: Context) {

    private val cache = mutableMapOf<String, Any>()
    private val loadingQueue = ConcurrentLinkedQueue<AssetRequest>()
    private val assetLoader = AssetLoader(context)

    var loadedCount: Int = 0
        private set
    var isLoading: Boolean = false
        private set

    fun <T> load(assetPath: String, type: AssetType, callback: (T?) -> Unit) {
        val existing = cache[assetPath]
        if (existing != null) {
            @Suppress("UNCHECKED_CAST")
            callback(existing as? T)
            return
        }
        isLoading = true
        assetLoader.loadAsync(assetPath, type) { result ->
            if (result != null) {
                cache[assetPath] = result as Any
                loadedCount++
            }
            @Suppress("UNCHECKED_CAST")
            callback(result as? T)
            isLoading = loadingQueue.isNotEmpty()
        }
    }

    fun <T> loadSync(assetPath: String, type: AssetType): T? {
        val existing = cache[assetPath]
        if (existing != null) {
            @Suppress("UNCHECKED_CAST")
            return existing as? T
        }
        @Suppress("UNCHECKED_CAST")
        val result = loadAsset<Any>(assetPath, type)
        if (result != null) {
            cache[assetPath] = result as Any
            loadedCount++
        }
        @Suppress("UNCHECKED_CAST")
        return result as? T
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(assetPath: String): T? {
        return cache[assetPath] as? T
    }

    fun unload(assetPath: String) {
        val removed = cache.remove(assetPath)
        if (removed != null) {
            if (removed is Bitmap) {
                if (!removed.isRecycled) {
                    removed.recycle()
                }
            }
            loadedCount--
        }
    }

    fun update() {
        while (loadingQueue.isNotEmpty()) {
            val request = loadingQueue.poll() ?: break
            @Suppress("UNCHECKED_CAST")
            val result = loadAsset<Any>(request.assetPath, request.type)
            @Suppress("UNCHECKED_CAST")
            (request.callback as (Any?) -> Unit)(result)
        }
    }

    fun unloadAll() {
        for ((_, value) in cache) {
            if (value is Bitmap && !value.isRecycled) {
                value.recycle()
            }
        }
        cache.clear()
        loadedCount = 0
    }

    fun contains(assetPath: String): Boolean = cache.containsKey(assetPath)

    @Suppress("UNCHECKED_CAST")
    private fun <T> loadAsset(assetPath: String, type: AssetType): T? {
        return when (type) {
            AssetType.TEXTURE -> assetLoader.loadTexture(assetPath) as? T
            AssetType.AUDIO -> assetLoader.loadBinary(assetPath) as? T
            AssetType.JSON -> assetLoader.loadText(assetPath) as? T
            AssetType.SHADER -> assetLoader.loadText(assetPath) as? T
        }
    }

    fun shutdown() {
        unloadAll()
        assetLoader.shutdown()
    }
}

data class AssetRequest(
    val assetPath: String,
    val type: AssetType,
    val callback: (Any?) -> Unit
)

enum class AssetType {
    TEXTURE,
    AUDIO,
    JSON,
    SHADER
}
