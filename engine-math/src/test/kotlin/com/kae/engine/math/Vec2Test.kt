package com.kae.engine.math

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import kotlin.math.PI
import kotlin.math.sqrt

class Vec2Test {

    @Test
    fun `test default constructor`() {
        val v = Vec2()
        assertEquals(0f, v.x)
        assertEquals(0f, v.y)
    }

    @Test
    fun `test parameterized constructor`() {
        val v = Vec2(3f, 4f)
        assertEquals(3f, v.x)
        assertEquals(4f, v.y)
    }

    @Test
    fun `test copy constructor`() {
        val a = Vec2(1f, 2f)
        val b = a.copy()
        assertEquals(a, b)
    }

    @Test
    fun `test companion constants`() {
        assertEquals(Vec2(0f, 0f), Vec2.ZERO)
        assertEquals(Vec2(1f, 1f), Vec2.ONE)
        assertEquals(Vec2(0f, 1f), Vec2.UP)
        assertEquals(Vec2(0f, -1f), Vec2.DOWN)
        assertEquals(Vec2(-1f, 0f), Vec2.LEFT)
        assertEquals(Vec2(1f, 0f), Vec2.RIGHT)
    }

    @Test
    fun `test unary minus`() {
        val v = Vec2(3f, -4f)
        val neg = -v
        assertEquals(Vec2(-3f, 4f), neg)
    }

    @Test
    fun `test plus`() {
        assertEquals(Vec2(5f, 7f), Vec2(1f, 2f) + Vec2(4f, 5f))
    }

    @Test
    fun `test minus`() {
        assertEquals(Vec2(-3f, -3f), Vec2(1f, 2f) - Vec2(4f, 5f))
    }

    @Test
    fun `test times scalar`() {
        assertEquals(Vec2(6f, 8f), Vec2(3f, 4f) * 2f)
    }

    @Test
    fun `test times scalar left`() {
        assertEquals(Vec2(6f, 8f), 2f * Vec2(3f, 4f))
    }

    @Test
    fun `test times component-wise`() {
        assertEquals(Vec2(3f, 8f), Vec2(1f, 2f) * Vec2(3f, 4f))
    }

    @Test
    fun `test div scalar`() {
        assertEquals(Vec2(2f, 3f), Vec2(4f, 6f) / 2f)
    }

    @Test
    fun `test div component-wise`() {
        assertEquals(Vec2(2f, 2f), Vec2(4f, 6f) / Vec2(2f, 3f))
    }

    @Test
    fun `test dot product`() {
        assertEquals(11f, Vec2(1f, 2f).dot(Vec2(3f, 4f)))
    }

    @Test
    fun `test cross product`() {
        assertEquals(-2f, Vec2(1f, 2f).cross(Vec2(3f, 4f)))
    }

    @Test
    fun `test length`() {
        assertEquals(5f, Vec2(3f, 4f).length())
    }

    @Test
    fun `test length squared`() {
        assertEquals(25f, Vec2(3f, 4f).lengthSquared())
    }

    @Test
    fun `test normalized`() {
        val v = Vec2(3f, 4f).normalized()
        assertEquals(1f, v.length(), 1e-6f)
        assertEquals(0.6f, v.x, 1e-6f)
        assertEquals(0.8f, v.y, 1e-6f)
    }

    @Test
    fun `test normalize zero vector`() {
        assertEquals(Vec2.ZERO, Vec2.ZERO.normalized())
    }

    @Test
    fun `test distance to`() {
        assertEquals(5f, Vec2(0f, 0f).distanceTo(Vec2(3f, 4f)))
    }

    @Test
    fun `test distance squared to`() {
        assertEquals(25f, Vec2(0f, 0f).distanceSquaredTo(Vec2(3f, 4f)))
    }

    @Test
    fun `test lerp`() {
        val v = Vec2(0f, 0f).lerp(Vec2(10f, 10f), 0.5f)
        assertEquals(Vec2(5f, 5f), v)
    }

    @Test
    fun `test reflect`() {
        val v = Vec2(1f, -1f)
        val normal = Vec2(0f, 1f)
        val reflected = v.reflect(normal)
        assertEquals(Vec2(1f, 1f), reflected)
    }

    @Test
    fun `test angle`() {
        assertEquals(0f, Vec2.RIGHT.angle(), 1e-6f)
        assertEquals(PI.toFloat() / 2f, Vec2.UP.angle(), 1e-6f)
    }

    @Test
    fun `test rotate`() {
        val v = Vec2(1f, 0f)
        val rotated = v.rotate(PI.toFloat() / 2f)
        assertEquals(0f, rotated.x, 1e-6f)
        assertEquals(1f, rotated.y, 1e-6f)
    }

    @Test
    fun `test perpendicular`() {
        val v = Vec2(1f, 0f)
        val perp = v.perpendicular()
        assertEquals(0f, perp.x, 1e-6f)
        assertEquals(1f, perp.y, 1e-6f)
    }

    @Test
    fun `test destructuring`() {
        val (x, y) = Vec2(3f, 4f)
        assertEquals(3f, x)
        assertEquals(4f, y)
    }

    @Test
    fun `test equals and hashCode`() {
        val a = Vec2(1f, 2f)
        val b = Vec2(1f, 2f)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `test toString`() {
        assertEquals("Vec2(3.0, 4.0)", Vec2(3f, 4f).toString())
    }
}
