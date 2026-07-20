package com.kae.engine.math

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import kotlin.math.PI
import kotlin.math.sqrt

class Vec3Test {

    @Test
    fun `test default constructor`() {
        val v = Vec3()
        assertEquals(0f, v.x)
        assertEquals(0f, v.y)
        assertEquals(0f, v.z)
    }

    @Test
    fun `test parameterized constructor`() {
        val v = Vec3(1f, 2f, 3f)
        assertEquals(1f, v.x)
        assertEquals(2f, v.y)
        assertEquals(3f, v.z)
    }

    @Test
    fun `test companion constants`() {
        assertEquals(Vec3(0f, 0f, 0f), Vec3.ZERO)
        assertEquals(Vec3(1f, 1f, 1f), Vec3.ONE)
        assertEquals(Vec3(0f, 1f, 0f), Vec3.UP)
        assertEquals(Vec3(0f, -1f, 0f), Vec3.DOWN)
        assertEquals(Vec3(1f, 0f, 0f), Vec3.RIGHT)
        assertEquals(Vec3(-1f, 0f, 0f), Vec3.LEFT)
        assertEquals(Vec3(0f, 0f, -1f), Vec3.FORWARD)
        assertEquals(Vec3(0f, 0f, 1f), Vec3.BACK)
        assertEquals(Vec3(0f, 0f, 1f), Vec3.Z_UP)
    }

    @Test
    fun `test unary minus`() {
        val v = Vec3(1f, -2f, 3f)
        assertEquals(Vec3(-1f, 2f, -3f), -v)
    }

    @Test
    fun `test plus`() {
        assertEquals(Vec3(4f, 6f, 8f), Vec3(1f, 2f, 3f) + Vec3(3f, 4f, 5f))
    }

    @Test
    fun `test minus`() {
        assertEquals(Vec3(-2f, -2f, -2f), Vec3(1f, 2f, 3f) - Vec3(3f, 4f, 5f))
    }

    @Test
    fun `test times scalar`() {
        assertEquals(Vec3(2f, 4f, 6f), Vec3(1f, 2f, 3f) * 2f)
    }

    @Test
    fun `test times scalar left`() {
        assertEquals(Vec3(2f, 4f, 6f), 2f * Vec3(1f, 2f, 3f))
    }

    @Test
    fun `test times component-wise`() {
        assertEquals(Vec3(3f, 8f, 15f), Vec3(1f, 2f, 3f) * Vec3(3f, 4f, 5f))
    }

    @Test
    fun `test div scalar`() {
        assertEquals(Vec3(1f, 2f, 3f), Vec3(2f, 4f, 6f) / 2f)
    }

    @Test
    fun `test dot product`() {
        assertEquals(14f, Vec3(1f, 2f, 3f).dot(Vec3(1f, 2f, 3f)))
    }

    @Test
    fun `test cross product`() {
        // i × j = k
        assertEquals(Vec3(0f, 0f, 1f), Vec3(1f, 0f, 0f).cross(Vec3(0f, 1f, 0f)))
        // j × k = i
        assertEquals(Vec3(1f, 0f, 0f), Vec3(0f, 1f, 0f).cross(Vec3(0f, 0f, 1f)))
        // k × i = j
        assertEquals(Vec3(0f, 1f, 0f), Vec3(0f, 0f, 1f).cross(Vec3(1f, 0f, 0f)))
        // Anti-commutativity
        val a = Vec3(1f, 2f, 3f)
        val b = Vec3(4f, 5f, 6f)
        assertEquals(-a.cross(b), b.cross(a))
    }

    @Test
    fun `test length`() {
        assertEquals(12.529964f, Vec3(2f, 3f, 12f).length(), 1e-4f)
    }

    @Test
    fun `test length squared`() {
        assertEquals(157f, Vec3(2f, 3f, 12f).lengthSquared())
    }

    @Test
    fun `test normalized`() {
        val v = Vec3(0f, 3f, 4f).normalized()
        assertEquals(1f, v.length(), 1e-6f)
        assertEquals(0f, v.x, 1e-6f)
        assertEquals(0.6f, v.y, 1e-6f)
        assertEquals(0.8f, v.z, 1e-6f)
    }

    @Test
    fun `test distance to`() {
        assertEquals(5f, Vec3(0f, 0f, 0f).distanceTo(Vec3(3f, 4f, 0f)))
    }

    @Test
    fun `test distance squared to`() {
        assertEquals(25f, Vec3(0f, 0f, 0f).distanceSquaredTo(Vec3(3f, 4f, 0f)))
    }

    @Test
    fun `test lerp`() {
        val v = Vec3(0f, 0f, 0f).lerp(Vec3(10f, 10f, 10f), 0.5f)
        assertEquals(Vec3(5f, 5f, 5f), v)
    }

    @Test
    fun `test reflect`() {
        val v = Vec3(1f, -1f, 0f)
        val normal = Vec3(0f, 1f, 0f)
        assertEquals(Vec3(1f, 1f, 0f), v.reflect(normal))
    }

    @Test
    fun `test xy component`() {
        assertEquals(Vec2(1f, 2f), Vec3(1f, 2f, 3f).xy())
    }

    @Test
    fun `test xz component`() {
        assertEquals(Vec2(1f, 3f), Vec3(1f, 2f, 3f).xz())
    }

    @Test
    fun `test yz component`() {
        assertEquals(Vec2(2f, 3f), Vec3(1f, 2f, 3f).yz())
    }

    @Test
    fun `test destructuring`() {
        val (x, y, z) = Vec3(1f, 2f, 3f)
        assertEquals(1f, x)
        assertEquals(2f, y)
        assertEquals(3f, z)
    }

    @Test
    fun `test equals and hashCode`() {
        val a = Vec3(1f, 2f, 3f)
        val b = Vec3(1f, 2f, 3f)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `test toString`() {
        assertEquals("Vec3(1.0, 2.0, 3.0)", Vec3(1f, 2f, 3f).toString())
    }
}
