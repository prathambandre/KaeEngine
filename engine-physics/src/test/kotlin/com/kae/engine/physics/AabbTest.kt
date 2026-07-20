package com.kae.engine.physics

import com.kae.engine.math.Vec2
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AabbTest {

    @Test
    fun `test AABB intersection - overlapping boxes`() {
        val a = AABB(Vec2(0f, 0f), Vec2(2f, 2f))
        val b = AABB(Vec2(1f, 1f), Vec2(3f, 3f))
        assertTrue(a.intersects(b))
        assertTrue(b.intersects(a))
    }

    @Test
    fun `test AABB intersection - non-overlapping boxes`() {
        val a = AABB(Vec2(0f, 0f), Vec2(1f, 1f))
        val b = AABB(Vec2(2f, 2f), Vec2(3f, 3f))
        assertFalse(a.intersects(b))
        assertFalse(b.intersects(a))
    }

    @Test
    fun `test AABB intersection - touching edges`() {
        val a = AABB(Vec2(0f, 0f), Vec2(1f, 1f))
        val b = AABB(Vec2(1f, 0f), Vec2(2f, 1f))
        assertTrue(a.intersects(b))
    }

    @Test
    fun `test AABB intersection - identical boxes`() {
        val a = AABB(Vec2(0f, 0f), Vec2(2f, 2f))
        val b = AABB(Vec2(0f, 0f), Vec2(2f, 2f))
        assertTrue(a.intersects(b))
    }

    @Test
    fun `test AABB contains another AABB`() {
        val outer = AABB(Vec2(0f, 0f), Vec2(10f, 10f))
        val inner = AABB(Vec2(2f, 2f), Vec2(5f, 5f))
        assertTrue(outer.contains(inner))
        assertFalse(inner.contains(outer))
    }

    @Test
    fun `test AABB contains point`() {
        val box = AABB(Vec2(0f, 0f), Vec2(2f, 2f))
        assertTrue(box.containsPoint(Vec2(1f, 1f)))
        assertTrue(box.containsPoint(Vec2(0f, 0f)))
        assertTrue(box.containsPoint(Vec2(2f, 2f)))
        assertFalse(box.containsPoint(Vec2(-1f, 0f)))
        assertFalse(box.containsPoint(Vec2(3f, 1f)))
    }

    @Test
    fun `test AABB merge`() {
        val a = AABB(Vec2(0f, 0f), Vec2(2f, 2f))
        val b = AABB(Vec2(3f, 3f), Vec2(5f, 5f))
        val merged = a.merge(b)
        assertEquals(Vec2(0f, 0f), merged.min)
        assertEquals(Vec2(5f, 5f), merged.max)
    }

    @Test
    fun `test AABB expand with point`() {
        val box = AABB(Vec2(1f, 1f), Vec2(3f, 3f))
        val expanded = box.expand(Vec2(5f, 0f))
        assertEquals(Vec2(1f, 0f), expanded.min)
        assertEquals(Vec2(5f, 3f), expanded.max)
    }

    @Test
    fun `test AABB area`() {
        val box = AABB(Vec2(0f, 0f), Vec2(3f, 4f))
        assertEquals(12f, box.area(), 0.001f)
    }

    @Test
    fun `test AABB perimeter`() {
        val box = AABB(Vec2(0f, 0f), Vec2(3f, 4f))
        assertEquals(14f, box.perimeter(), 0.001f)
    }

    @Test
    fun `test AABB overlap - intersecting`() {
        val a = AABB(Vec2(0f, 0f), Vec2(2f, 2f))
        val b = AABB(Vec2(1f, 1f), Vec2(3f, 3f))
        val overlap = a.overlap(b)
        assertNotNull(overlap)
        assertEquals(Vec2(1f, 1f), overlap!!.min)
        assertEquals(Vec2(2f, 2f), overlap.max)
    }

    @Test
    fun `test AABB overlap - no intersection`() {
        val a = AABB(Vec2(0f, 0f), Vec2(1f, 1f))
        val b = AABB(Vec2(2f, 2f), Vec2(3f, 3f))
        assertNull(a.overlap(b))
    }

    @Test
    fun `test AABB center and size`() {
        val box = AABB(Vec2(2f, 4f), Vec2(6f, 8f))
        assertEquals(Vec2(4f, 6f), box.center)
        assertEquals(Vec2(4f, 4f), box.size)
        assertEquals(Vec2(2f, 2f), box.halfSize)
    }

    @Test
    fun `test fromCenterHalf`() {
        val box = AABB.fromCenterHalf(Vec2(5f, 5f), Vec2(2f, 3f))
        assertEquals(Vec2(3f, 2f), box.min)
        assertEquals(Vec2(7f, 8f), box.max)
    }

    @Test
    fun `test fromPoints`() {
        val points = listOf(Vec2(1f, 5f), Vec2(3f, 2f), Vec2(0f, 0f), Vec2(4f, 6f))
        val box = AABB.fromPoints(points)
        assertNotNull(box)
        assertEquals(Vec2(0f, 0f), box!!.min)
        assertEquals(Vec2(4f, 6f), box.max)
    }

    @Test
    fun `test fromPoints empty list`() {
        assertNull(AABB.fromPoints(emptyList()))
    }

    @Test
    fun `test AABB contains point on boundary`() {
        val box = AABB(Vec2(0f, 0f), Vec2(2f, 2f))
        assertTrue(box.containsPoint(Vec2(0f, 0f)))
        assertTrue(box.containsPoint(Vec2(2f, 0f)))
        assertTrue(box.containsPoint(Vec2(0f, 2f)))
        assertTrue(box.containsPoint(Vec2(2f, 2f)))
    }

    @Test
    fun `test AABB intersection - partially overlapping`() {
        val a = AABB(Vec2(0f, 0f), Vec2(3f, 3f))
        val b = AABB(Vec2(2f, 2f), Vec2(5f, 5f))
        assertTrue(a.intersects(b))
        val overlap = a.overlap(b)
        assertNotNull(overlap)
        assertEquals(Vec2(2f, 2f), overlap!!.min)
        assertEquals(Vec2(3f, 3f), overlap.max)
    }

    @Test
    fun `test AABB intersection - one contains other`() {
        val outer = AABB(Vec2(0f, 0f), Vec2(10f, 10f))
        val inner = AABB(Vec2(3f, 3f), Vec2(7f, 7f))
        assertTrue(outer.intersects(inner))
        assertTrue(inner.intersects(outer))
    }

    @Test
    fun `test merge overlapping boxes`() {
        val a = AABB(Vec2(0f, 0f), Vec2(3f, 3f))
        val b = AABB(Vec2(1f, 1f), Vec2(4f, 4f))
        val merged = a.merge(b)
        assertEquals(Vec2(0f, 0f), merged.min)
        assertEquals(Vec2(4f, 4f), merged.max)
    }
}
