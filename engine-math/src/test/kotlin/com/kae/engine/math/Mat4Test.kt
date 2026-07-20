package com.kae.engine.math

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Mat4Test {

    private fun assertMat4Equals(expected: Mat4, actual: Mat4, epsilon: Float = 1e-4f) {
        for (row in 0..3) {
            for (col in 0..3) {
                assertEquals(
                    expected[row, col], actual[row, col], epsilon,
                    "Mismatch at [$row,$col]: expected ${expected[row, col]} but got ${actual[row, col]}"
                )
            }
        }
    }

    @Test
    fun `test identity matrix`() {
        val m = Mat4.identity()
        assertEquals(1f, m[0, 0])
        assertEquals(1f, m[1, 1])
        assertEquals(1f, m[2, 2])
        assertEquals(1f, m[3, 3])
        assertEquals(0f, m[0, 1])
        assertEquals(0f, m[1, 0])
        assertEquals(0f, m[2, 3])
    }

    @Test
    fun `test identity times identity`() {
        val i = Mat4.identity()
        assertMat4Equals(i, i * i)
    }

    @Test
    fun `test translation matrix`() {
        val t = Mat4.translation(1f, 2f, 3f)
        assertEquals(1f, t[0, 0])
        assertEquals(1f, t[1, 1])
        assertEquals(1f, t[2, 2])
        assertEquals(1f, t[3, 3])
        assertEquals(1f, t[0, 3])
        assertEquals(2f, t[1, 3])
        assertEquals(3f, t[2, 3])
    }

    @Test
    fun `test translation transforms point`() {
        val t = Mat4.translation(5f, 10f, 15f)
        val p = t.transformPoint(Vec3(1f, 2f, 3f))
        assertEquals(6f, p.x, 1e-5f)
        assertEquals(12f, p.y, 1e-5f)
        assertEquals(18f, p.z, 1e-5f)
    }

    @Test
    fun `test scale matrix`() {
        val s = Mat4.scale(2f, 3f, 4f)
        val p = s.transformPoint(Vec3(1f, 1f, 1f))
        assertEquals(2f, p.x, 1e-5f)
        assertEquals(3f, p.y, 1e-5f)
        assertEquals(4f, p.z, 1e-5f)
    }

    @Test
    fun `test rotation around Y axis`() {
        val angle = PI.toFloat() / 2f
        val r = Mat4.rotation(angle, Vec3.UP)
        val p = r.transformPoint(Vec3(1f, 0f, 0f))
        assertEquals(0f, p.x, 1e-4f)
        assertEquals(0f, p.y, 1e-4f)
        assertEquals(-1f, p.z, 1e-4f)
    }

    @Test
    fun `test rotation around Z axis`() {
        val angle = PI.toFloat() / 2f
        val r = Mat4.rotation(angle, Vec3.Z_UP)
        val p = r.transformPoint(Vec3(1f, 0f, 0f))
        assertEquals(0f, p.x, 1e-4f)
        assertEquals(1f, p.y, 1e-4f)
        assertEquals(0f, p.z, 1e-4f)
    }

    @Test
    fun `test matrix multiplication order`() {
        val t = Mat4.translation(5f, 0f, 0f)
        val s = Mat4.scale(2f, 2f, 2f)
        // Apply scale first, then translation
        val ts = t * s
        val p = ts.transformPoint(Vec3(1f, 0f, 0f))
        assertEquals(7f, p.x, 1e-5f) // 1*2 + 5
        assertEquals(0f, p.y, 1e-5f)
        assertEquals(0f, p.z, 1e-5f)
    }

    @Test
    fun `test inverse of translation`() {
        val t = Mat4.translation(5f, 10f, 15f)
        val inv = t.inverse()
        val p = t.transformPoint(Vec3(1f, 2f, 3f))
        val p2 = inv.transformPoint(p)
        assertEquals(1f, p2.x, 1e-4f)
        assertEquals(2f, p2.y, 1e-4f)
        assertEquals(3f, p2.z, 1e-4f)
    }

    @Test
    fun `test inverse of scale`() {
        val s = Mat4.scale(2f, 4f, 8f)
        val inv = s.inverse()
        val p = s.transformPoint(Vec3(1f, 1f, 1f))
        val p2 = inv.transformPoint(p)
        assertEquals(1f, p2.x, 1e-4f)
        assertEquals(1f, p2.y, 1e-4f)
        assertEquals(1f, p2.z, 1e-4f)
    }

    @Test
    fun `test matrix times inverse is identity`() {
        val m = Mat4.translation(1f, 2f, 3f) * Mat4.scale(2f, 3f, 4f)
        val inv = m.inverse()
        val result = m * inv
        assertMat4Equals(Mat4.identity(), result)
    }

    @Test
    fun `test determinant of identity`() {
        assertEquals(1f, Mat4.identity().determinant())
    }

    @Test
    fun `test determinant of scale`() {
        val s = Mat4.scale(2f, 3f, 4f)
        assertEquals(24f, s.determinant(), 1e-4f)
    }

    @Test
    fun `test transpose`() {
        val m = Mat4.translation(1f, 2f, 3f)
        val t = m.transpose()
        assertEquals(1f, t[0, 0])
        assertEquals(1f, t[3, 0]) // was at [0,3]
        assertEquals(2f, t[3, 1]) // was at [1,3]
        assertEquals(3f, t[3, 2]) // was at [2,3]
    }

    @Test
    fun `test double transpose is identity`() {
        val m = Mat4.translation(1f, 2f, 3f) * Mat4.scale(2f, 3f, 4f)
        assertMat4Equals(m, m.transpose().transpose())
    }

    @Test
    fun `test orthographic projection`() {
        val ortho = Mat4.ortho(-1f, 1f, -1f, 1f, 0.1f, 100f)
        // A point at (0, 0, -1) should map to (0, 0, ~-1 in NDC, 1)
        val p = ortho.transform(Vec4(0f, 0f, -1f, 1f))
        // In ortho: z_ndc = (-2/(far-near))*z - (far+near)/(far-near)
        // For z = -1, near=0.1, far=100:
        // z_ndc = (-2/99.9)*(-1) - (100.1/99.9) = 0.02002 - 1.002 = -0.982
        assertEquals(0f, p.x / p.w, 1e-4f)
        assertEquals(0f, p.y / p.w, 1e-4f)
    }

    @Test
    fun `test perspective projection preserves w`() {
        val persp = Mat4.perspective(PI.toFloat() / 3f, 16f / 9f, 0.1f, 100f)
        // A point on the z-axis should have w = -z
        val p = persp.transform(Vec4(0f, 0f, -10f, 1f))
        assertEquals(0f, p.x, 1e-4f)
        assertEquals(0f, p.y, 1e-4f)
        assertEquals(10f, p.w, 1e-4f) // w = -z = 10
    }

    @Test
    fun `test lookAt matrix`() {
        val eye = Vec3(0f, 0f, 5f)
        val center = Vec3(0f, 0f, 0f)
        val up = Vec3(0f, 1f, 0f)
        val view = Mat4.lookAt(eye, center, up)

        // A point at origin should be at (0, 0, -5) in view space
        val p = view.transformPoint(Vec3(0f, 0f, 0f))
        assertEquals(0f, p.x, 1e-4f)
        assertEquals(0f, p.y, 1e-4f)
        assertEquals(-5f, p.z, 1e-4f)
    }

    @Test
    fun `test getTranslation`() {
        val t = Mat4.translation(5f, 10f, 15f)
        assertEquals(Vec3(5f, 10f, 15f), t.getTranslation())
    }

    @Test
    fun `test getScale`() {
        val s = Mat4.scale(2f, 3f, 4f)
        assertEquals(Vec3(2f, 3f, 4f), s.getScale())
    }

    @Test
    fun `test transformPoint vs transform with w1`() {
        val m = Mat4.translation(1f, 2f, 3f)
        val v3 = Vec3(4f, 5f, 6f)
        val v4 = Vec4(4f, 5f, 6f, 1f)
        val fromPoint = m.transformPoint(v3)
        val fromVec4 = m.transform(v4)
        assertEquals(fromPoint.x, fromVec4.x, 1e-5f)
        assertEquals(fromPoint.y, fromVec4.y, 1e-5f)
        assertEquals(fromPoint.z, fromVec4.z, 1e-5f)
    }

    @Test
    fun `test transformDirection ignores translation`() {
        val m = Mat4.translation(100f, 200f, 300f) * Mat4.scale(2f, 2f, 2f)
        val dir = m.transformDirection(Vec3(1f, 0f, 0f))
        assertEquals(2f, dir.x, 1e-5f)
        assertEquals(0f, dir.y, 1e-5f)
        assertEquals(0f, dir.z, 1e-5f)
    }

    @Test
    fun `test toFloatArray`() {
        val m = Mat4.identity()
        val arr = m.toFloatArray()
        assertEquals(16, arr.size)
        assertEquals(1f, arr[0])
        assertEquals(1f, arr[5])
        assertEquals(1f, arr[10])
        assertEquals(1f, arr[15])
    }
}
