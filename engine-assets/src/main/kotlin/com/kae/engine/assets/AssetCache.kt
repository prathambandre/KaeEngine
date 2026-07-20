package com.kae.engine.assets

import android.util.LruCache

class AssetCache(private val maxSize: Int = 100) {

    private val lruCache = object : LruCache<String, Any>(maxSize) {
        override fun sizeOf(key: String, value: Any): Int = 1
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String): T? {
        return lruCache.get(key) as? T
    }

    fun <T> put(key: String, asset: T) {
        lruCache.put(key, asset as Any)
    }

    fun remove(key: String): Boolean {
        val removed = lruCache.remove(key)
        return removed != null
    }

    fun contains(key: String): Boolean {
        return lruCache.get(key) != null
    }

    fun clear() {
        lruCache.evictAll()
    }

    val size: Int
        get() = lruCache.size()
}
