package com.kae.engine.core

class ObjectPool<T>(
    private val factory: () -> T,
    initialCapacity: Int = 16
) {
    private val pool: ArrayDeque<T> = ArrayDeque(initialCapacity)
    private var activeCount: Int = 0

    fun obtain(): T {
        val obj = if (pool.isNotEmpty()) pool.removeFirst() else factory()
        activeCount++
        return obj
    }

    fun free(obj: T) {
        if (activeCount > 0) {
            activeCount--
            pool.addLast(obj)
        }
    }

    fun freeAll(objects: Iterable<T>) {
        for (obj in objects) {
            free(obj)
        }
    }

    fun freeAll() {
        activeCount = 0
        pool.clear()
    }

    fun getPoolSize(): Int = pool.size

    fun getActiveCount(): Int = activeCount

    fun getTotalCount(): Int = pool.size + activeCount

    fun preload(count: Int) {
        repeat(count) {
            pool.addLast(factory())
        }
    }

    fun clear() {
        pool.clear()
        activeCount = 0
    }
}
