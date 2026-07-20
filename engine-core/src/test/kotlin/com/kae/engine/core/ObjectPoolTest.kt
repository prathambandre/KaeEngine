package com.kae.engine.core

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ObjectPoolTest {

    @Test
    fun `obtain returns new object from factory when pool is empty`() {
        var callCount = 0
        val pool = ObjectPool(factory = { callCount++ })
        val obj = pool.obtain()
        assertEquals(0, obj)
        assertEquals(1, callCount)
    }

    @Test
    fun `obtain reuses freed object`() {
        val pool = ObjectPool(factory = { StringBuilder() })
        val obj1 = pool.obtain()
        obj1.append("hello")
        pool.free(obj1)

        val obj2 = pool.obtain()
        assertSame(obj1, obj2)
        assertEquals("hello", obj2.toString())
    }

    @Test
    fun `free decrements active count and increases pool size`() {
        val pool = ObjectPool(factory = { Any() })
        val obj = pool.obtain()
        assertEquals(1, pool.getActiveCount())
        assertEquals(0, pool.getPoolSize())

        pool.free(obj)
        assertEquals(0, pool.getActiveCount())
        assertEquals(1, pool.getPoolSize())
    }

    @Test
    fun `obtain increments active count`() {
        val pool = ObjectPool(factory = { Any() })
        pool.obtain()
        pool.obtain()
        assertEquals(2, pool.getActiveCount())
        assertEquals(0, pool.getPoolSize())
    }

    @Test
    fun `freeAll with iterable frees multiple objects`() {
        val pool = ObjectPool(factory = { StringBuilder() })
        val objects = (1..5).map { pool.obtain() }
        assertEquals(5, pool.getActiveCount())

        pool.freeAll(objects)
        assertEquals(0, pool.getActiveCount())
        assertEquals(5, pool.getPoolSize())
    }

    @Test
    fun `freeAll without args clears pool`() {
        val pool = ObjectPool(factory = { Any() })
        pool.obtain()
        pool.obtain()
        pool.obtain()
        pool.freeAll()
        assertEquals(0, pool.getActiveCount())
        assertEquals(0, pool.getPoolSize())
    }

    @Test
    fun `preload adds objects to pool`() {
        val pool = ObjectPool(factory = { StringBuilder() })
        pool.preload(10)
        assertEquals(10, pool.getPoolSize())
        assertEquals(0, pool.getActiveCount())

        repeat(10) { pool.obtain() }
        assertEquals(0, pool.getPoolSize())
        assertEquals(10, pool.getActiveCount())
    }

    @Test
    fun `getTotalCount returns sum of pool and active`() {
        val pool = ObjectPool(factory = { Any() })
        pool.preload(5)
        val obj1 = pool.obtain()
        val obj2 = pool.obtain()
        assertEquals(5, pool.getTotalCount())
    }

    @Test
    fun `clear resets all state`() {
        val pool = ObjectPool(factory = { Any() })
        pool.preload(10)
        pool.obtain()
        pool.obtain()
        pool.clear()
        assertEquals(0, pool.getPoolSize())
        assertEquals(0, pool.getActiveCount())
        assertEquals(0, pool.getTotalCount())
    }

    @Test
    fun `free does nothing when active count is zero`() {
        val pool = ObjectPool(factory = { Any() })
        val obj = Any()
        pool.free(obj)
        assertEquals(0, pool.getPoolSize())
    }

    @Test
    fun `factory is called only when pool is empty`() {
        var callCount = 0
        val pool = ObjectPool(factory = { callCount++ })

        pool.preload(3)
        assertEquals(3, callCount)

        pool.obtain()
        pool.obtain()
        pool.obtain()
        assertEquals(3, callCount)

        pool.obtain()
        assertEquals(4, callCount)
    }
}
