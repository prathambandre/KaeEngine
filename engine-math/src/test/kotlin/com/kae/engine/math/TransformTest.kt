package com.kae.engine.math

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import kotlin.math.PI
import kotlin.math.abs

class TransformTest {

    private fun assertVec3Equals(expected: Vec3, actual: Vec3, epsilon: Float = 1e-4f) {
        assertEquals(expected.x, actual.x, epsilon, "x mismatch")
        assertEquals(expected.y, actual.y, epsilon, "y mismatch")
        assertEquals(expected.z, actual.z, epsilon, "z mismatch")
    }

    @Test
    fun `test default transform is identity`() {
        val t = Transform()
        assertEquals(Vec3.ZERO, t.position)
        assertEquals(Quaternion.IDENTITY, t.rotation)
        assertEquals(Vec3.ONE, t.scale)
    }

    @Test
    fun `test toMatrix identity`() {
        val m = Transform().toMatrix()
        assertEquals(1f, m[0, 0])
        assertEquals(1f, m[1, 1])
        assertEquals(1f, m[2, 2])
        assertEquals(1f, m[3, 3])
        assertEquals(0f, m[0, 1])
        assertEquals(0f, m[0, 2])
        assertEquals(0f, m[0, 3])
    }

    @Test
    fun `test toMatrix with position`() {
        val t = Transform(position = Vec3(5f, 10f, 15f))
        val m = t.toMatrix()
        val p = m.transformPoint(Vec3.ZERO)
        assertVec3Equals(Vec3(5f, 10f, 15f), p)
    }

    @Test
    fun `test toMatrix with scale`() {
        val t = Transform(scale = Vec3(2f, 3f, 4f))
        val m = t.toMatrix()
        val p = m.transformPoint(Vec3(1f, 1f, 1f))
        assertVec3Equals(Vec3(2f, 3f, 4f), p)
    }

    @Test
    fun `test toMatrix with rotation`() {
        val t = Transform(rotation = Quaternion.fromAxisAngle(Vec3.Z_UP, PI.toFloat() / 2f))
        val m = t.toMatrix()
        val p = m.transformPoint(Vec3(1f, 0f, 0f))
        assertVec3Equals(Vec3(0f, 1f, 0f), p)
    }

    @Test
    fun `test forward default`() {
        val t = Transform()
        assertVec3Equals(Vec3.FORWARD, t.forward())
    }

    @Test
    fun `test forward rotated around Y`() {
        val t = Transform(rotation = Quaternion.fromAxisAngle(Vec3.UP, PI.toFloat() / 2f))
        // Rotating forward (0,0,-1) around Y by 90° should give (−1,0,0)
        assertVec3Equals(Vec3(-1f, 0f, 0f), t.forward())
    }

    @Test
    fun `test right default`() {
        val t = Transform()
        assertVec3Equals(Vec3.RIGHT, t.right())
    }

    @Test
    fun `test up default`() {
        val t = Transform()
        assertVec3Equals(Vec3.UP, t.up())
    }

    @Test
    fun `test translate`() {
        val t = Transform()
        t.translate(Vec3(1f, 2f, 3f))
        // translate in local space with no rotation should be same as world space
        assertVec3Equals(Vec3(1f, 2f, 3f), t.position)
    }

    @Test
    fun `test translate in rotated space`() {
        val t = Transform(rotation = Quaternion.fromAxisAngle(Vec3.UP, PI.toFloat() / 2f))
        t.translate(Vec3(1f, 0f, 0f))
        // Forward in local space is (1,0,0), which when rotated 90° around Y should become (0,0,-1)
        // Actually, local forward is (-1,0,0), translating (1,0,0) means translating in local X = right = (0,0,-1) after 90° Y rotation
        // Let me recalculate: rotation around Y by 90°:
        // right (1,0,0) -> (0,0,-1), forward (0,0,-1) -> (-1,0,0)
        // translate(1,0,0) rotates (1,0,0) by the quaternion: right becomes (0,0,-1)
        assertVec3Equals(Vec3(0f, 0f, -1f), t.position)
    }

    @Test
    fun `test rotate`() {
        val t = Transform()
        t.rotate(Vec3.Z_UP, PI.toFloat() / 2f)
        val forward = t.forward()
        // After rotating 90° around Z, forward (0,0,-1) should stay the same (Z axis is rotation axis)
        // Actually forward = q * (0,0,-1). For rotation around Z, the Z component is preserved.
        assertVec3Equals(Vec3(0f, 0f, -1f), forward)
    }

    @Test
    fun `test scaleBy`() {
        val t = Transform(scale = Vec3(2f, 2f, 2f))
        t.scaleBy(Vec3(3f, 4f, 5f))
        assertVec3Equals(Vec3(6f, 8f, 10f), t.scale)
    }

    @Test
    fun `test combined transform multiplication`() {
        val a = Transform(position = Vec3(1f, 0f, 0f))
        val b = Transform(position = Vec3(0f, 2f, 0f))
        val combined = a * b
        // a * b means: apply b first, then a
        // Result: position should be sum of positions
        assertVec3Equals(Vec3(1f, 2f, 0f), combined.position)
    }

    @Test
    fun `test combined transform preserves rotation`() {
        val a = Transform(rotation = Quaternion.fromAxisAngle(Vec3.Z_UP, PI.toFloat() / 4f))
        val b = Transform(rotation = Quaternion.fromAxisAngle(Vec3.Z_UP, PI.toFloat() / 4f))
        val combined = a * b
        val forward = combined.forward()
        assertVec3Equals(Vec3(0f, 0f, -1f), forward)
    }

    @Test
    fun `test combined transform with scale and position`() {
        val a = Transform(position = Vec3(5f, 0f, 0f), scale = Vec3(2f, 2f, 2f))
        val b = Transform(position = Vec3(1f, 0f, 0f))
        val combined = a * b
        // b translates by (1,0,0), then a scales and translates
        val m = combined.toMatrix()
        val p = m.transformPoint(Vec3.ZERO)
        // b: (0,0,0) -> (1,0,0), then a: scale by 2 -> (2,0,0), translate by (5,0,0) -> (7,0,0)
        assertVec3Equals(Vec3(7f, 0f, 0f), p)
    }

    @Test
    fun `test transform roundtrip through matrix`() {
        val t = Transform(
            position = Vec3(10f, 20f, 30f),
            rotation = Quaternion.fromAxisAngle(Vec3.UP, PI.toFloat() / 3f),
            scale = Vec3(2f, 3f, 4f)
        )
        val m = t.toMatrix()
        // Point at origin should move to position
        assertVec3Equals(t.position, m.transformPoint(Vec3.ZERO))
    }

    @Test
    fun `test direction vectors are unit length`() {
        val t = Transform(rotation = Quaternion.fromEuler(PI.toFloat() / 4f, PI.toFloat() / 3f, PI.toFloat() / 6f))
        assertEquals(1f, t.forward().length(), 1e-4f)
        assertEquals(1f, t.right().length(), 1e-4f)
        assertEquals(1f, t.up().length(), 1e-4f)
    }

    @Test
    fun `test direction vectors are orthogonal`() {
        val t = Transform(rotation = Quaternion.fromEuler(PI.toFloat() / 4f, PI.toFloat() / 3f, PI.toFloat() / 6f))
        assertEquals(0f, t.forward().dot(t.right()), 1e-4f)
        assertEquals(0f, t.forward().dot(t.up()), 1e-4f)
        assertEquals(0f, t.right().dot(t.up()), 1e-4f)
    }
}
