package com.kae.engine.math

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class QuaternionTest {

    private fun assertVec3Equals(expected: Vec3, actual: Vec3, epsilon: Float = 1e-4f) {
        assertEquals(expected.x, actual.x, epsilon, "x mismatch")
        assertEquals(expected.y, actual.y, epsilon, "y mismatch")
        assertEquals(expected.z, actual.z, epsilon, "z mismatch")
    }

    private fun assertQuatEquals(expected: Quaternion, actual: Quaternion, epsilon: Float = 1e-4f) {
        assertEquals(expected.x, actual.x, epsilon, "x mismatch")
        assertEquals(expected.y, actual.y, epsilon, "y mismatch")
        assertEquals(expected.z, actual.z, epsilon, "z mismatch")
        assertEquals(expected.w, actual.w, epsilon, "w mismatch")
    }

    @Test
    fun `test default constructor is identity`() {
        val q = Quaternion()
        assertEquals(Quaternion.IDENTITY, q)
    }

    @Test
    fun `test identity quaternion`() {
        assertEquals(0f, Quaternion.IDENTITY.x)
        assertEquals(0f, Quaternion.IDENTITY.y)
        assertEquals(0f, Quaternion.IDENTITY.z)
        assertEquals(1f, Quaternion.IDENTITY.w)
    }

    @Test
    fun `test length of identity`() {
        assertEquals(1f, Quaternion.IDENTITY.length(), 1e-6f)
    }

    @Test
    fun `test length squared of identity`() {
        assertEquals(1f, Quaternion.IDENTITY.lengthSquared(), 1e-6f)
    }

    @Test
    fun `test normalized`() {
        val q = Quaternion(1f, 2f, 3f, 4f).normalized()
        assertEquals(1f, q.length(), 1e-5f)
    }

    @Test
    fun `test conjugate of identity`() {
        assertQuatEquals(Quaternion.IDENTITY, Quaternion.IDENTITY.conjugate())
    }

    @Test
    fun `test conjugate`() {
        val q = Quaternion(1f, 2f, 3f, 4f)
        val c = q.conjugate()
        assertEquals(-1f, c.x)
        assertEquals(-2f, c.y)
        assertEquals(-3f, c.z)
        assertEquals(4f, c.w)
    }

    @Test
    fun `test inverse`() {
        val q = Quaternion.fromAxisAngle(Vec3.UP, PI.toFloat() / 4f)
        val inv = q.inverse()
        val product = q * inv
        assertQuatEquals(Quaternion.IDENTITY, product)
    }

    @Test
    fun `test dot product`() {
        val a = Quaternion(1f, 2f, 3f, 4f)
        val b = Quaternion(5f, 6f, 7f, 8f)
        // 1*5 + 2*6 + 3*7 + 4*8 = 5+12+21+32 = 70
        assertEquals(70f, a.dot(b))
    }

    @Test
    fun `test quaternion multiplication identity`() {
        val q = Quaternion.fromAxisAngle(Vec3.UP, PI.toFloat() / 4f)
        assertQuatEquals(q, Quaternion.IDENTITY * q)
        assertQuatEquals(q, q * Quaternion.IDENTITY)
    }

    @Test
    fun `test rotate vector identity`() {
        val v = Vec3(1f, 2f, 3f)
        assertVec3Equals(v, Quaternion.IDENTITY * v)
    }

    @Test
    fun `test rotate vector around Z axis 90 degrees`() {
        val q = Quaternion.fromAxisAngle(Vec3.Z_UP, PI.toFloat() / 2f)
        val rotated = q * Vec3(1f, 0f, 0f)
        assertVec3Equals(Vec3(0f, 1f, 0f), rotated)
    }

    @Test
    fun `test rotate vector around Y axis 90 degrees`() {
        val q = Quaternion.fromAxisAngle(Vec3.UP, PI.toFloat() / 2f)
        val rotated = q * Vec3(0f, 0f, 1f)
        assertVec3Equals(Vec3(1f, 0f, 0f), rotated)
    }

    @Test
    fun `test rotate vector around X axis 90 degrees`() {
        val q = Quaternion.fromAxisAngle(Vec3.RIGHT, PI.toFloat() / 2f)
        val rotated = q * Vec3(0f, 1f, 0f)
        assertVec3Equals(Vec3(0f, 0f, 1f), rotated)
    }

    @Test
    fun `test fromAxisAngle zero rotation`() {
        val q = Quaternion.fromAxisAngle(Vec3.UP, 0f)
        assertQuatEquals(Quaternion.IDENTITY, q)
    }

    @Test
    fun `test fromEuler zero rotation`() {
        val q = Quaternion.fromEuler(0f, 0f, 0f)
        assertQuatEquals(Quaternion.IDENTITY, q)
    }

    @Test
    fun `test toMat4 of identity is identity`() {
        val m = Quaternion.IDENTITY.toMat4()
        assertEquals(1f, m[0, 0])
        assertEquals(1f, m[1, 1])
        assertEquals(1f, m[2, 2])
        assertEquals(1f, m[3, 3])
        assertEquals(0f, m[0, 1])
        assertEquals(0f, m[1, 0])
    }

    @Test
    fun `test toMat4 rotation around Z 90 degrees`() {
        val q = Quaternion.fromAxisAngle(Vec3.Z_UP, PI.toFloat() / 2f)
        val m = q.toMat4()
        // cos(90) = 0, sin(90) = 1
        // Should map (1,0,0) -> (0,1,0)
        val p = m.transformPoint(Vec3(1f, 0f, 0f))
        assertVec3Equals(Vec3(0f, 1f, 0f), p)
    }

    @Test
    fun `test slerp t0 is first`() {
        val a = Quaternion.fromAxisAngle(Vec3.UP, 0f)
        val b = Quaternion.fromAxisAngle(Vec3.UP, PI.toFloat())
        val result = Quaternion.slerp(a, b, 0f)
        assertQuatEquals(a, result)
    }

    @Test
    fun `test slerp t1 is second`() {
        val a = Quaternion.fromAxisAngle(Vec3.UP, 0f)
        val b = Quaternion.fromAxisAngle(Vec3.UP, PI.toFloat())
        val result = Quaternion.slerp(a, b, 1f)
        assertQuatEquals(b, result)
    }

    @Test
    fun `test slerp t05 is midpoint`() {
        val a = Quaternion.IDENTITY
        val b = Quaternion.fromAxisAngle(Vec3.UP, PI.toFloat() / 2f)
        val result = Quaternion.slerp(a, b, 0.5f)
        val v = result * Vec3(1f, 0f, 0f)
        assertVec3Equals(Vec3(0.7071f, 0f, -0.7071f), v, 1e-3f)
    }

    @Test
    fun `test toEuler from identity`() {
        val euler = Quaternion.IDENTITY.toEuler()
        assertVec3Equals(Vec3(0f, 0f, 0f), euler)
    }

    @Test
    fun `test toEuler from axis angle`() {
        val q = Quaternion.fromAxisAngle(Vec3.Z_UP, PI.toFloat() / 4f)
        val euler = q.toEuler()
        assertEquals(0f, euler.x, 1e-4f) // pitch
        assertEquals(0f, euler.y, 1e-4f) // yaw
        assertEquals(PI.toFloat() / 4f, euler.z, 1e-4f) // roll
    }

    @Test
    fun `test destructuring`() {
        val (x, y, z, w) = Quaternion(1f, 2f, 3f, 4f)
        assertEquals(1f, x)
        assertEquals(2f, y)
        assertEquals(3f, z)
        assertEquals(4f, w)
    }

    @Test
    fun `test nlerp t05`() {
        val a = Quaternion.IDENTITY
        val b = Quaternion.fromAxisAngle(Vec3.Z_UP, PI.toFloat())
        val result = a.nlerp(b, 0.5f)
        assertEquals(1f, result.length(), 1e-5f)
    }

    @Test
    fun `test angleBetween identity`() {
        assertEquals(0f, Quaternion.IDENTITY.angleBetween(Quaternion.IDENTITY), 1e-5f)
    }

    @Test
    fun `test equals and hashCode`() {
        val a = Quaternion(1f, 2f, 3f, 4f)
        val b = Quaternion(1f, 2f, 3f, 4f)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `test toString`() {
        assertEquals("Quaternion(1.0, 2.0, 3.0, 4.0)", Quaternion(1f, 2f, 3f, 4f).toString())
    }
}
