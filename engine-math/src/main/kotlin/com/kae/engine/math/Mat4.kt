package com.kae.engine.math

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * A 4x4 matrix stored in column-major order.
 *
 * Storage layout: elements[col * 4 + row]
 *
 * | m00 m01 m02 m03 |   | 0  4  8  12 |
 * | m10 m11 m12 m13 | = | 1  5  9  13 |
 * | m20 m21 m22 m23 |   | 2  6  10 14 |
 * | m30 m31 m32 m33 |   | 3  7  11 15 |
 */
data class Mat4(val elements: FloatArray = FloatArray(16).also { it[0] = 1f; it[5] = 1f; it[10] = 1f; it[15] = 1f }) {

    /**
     * Returns the element at the given [row] and [col].
     */
    operator fun get(row: Int, col: Int): Float = elements[col * 4 + row]

    /**
     * Sets the element at the given [row] and [col] to [value].
     */
    operator fun set(row: Int, col: Int, value: Float) {
        elements[col * 4 + row] = value
    }

    /**
     * Returns this matrix as a plain FloatArray (column-major, same as internal storage).
     */
    fun toFloatArray(): FloatArray = elements.copyOf()

    /**
     * Returns the translation component of this matrix as a Vec3.
     */
    fun getTranslation(): Vec3 = Vec3(elements[12], elements[13], elements[14])

    /**
     * Returns the scale component of this matrix as a Vec3.
     * Extracts the length of each column's xyz part.
     */
    fun getScale(): Vec3 = Vec3(
        sqrt(elements[0] * elements[0] + elements[1] * elements[1] + elements[2] * elements[2]),
        sqrt(elements[4] * elements[4] + elements[5] * elements[5] + elements[6] * elements[6]),
        sqrt(elements[8] * elements[8] + elements[9] * elements[9] + elements[10] * elements[10])
    )

    /**
     * Returns the rotation part of this matrix as a Mat4 (translation zeroed, scale removed).
     */
    fun getRotation(): Mat4 {
        val scale = getScale()
        val invScale = Vec3(
            if (kotlin.math.abs(scale.x) > 1e-6f) 1f / scale.x else 0f,
            if (kotlin.math.abs(scale.y) > 1e-6f) 1f / scale.y else 0f,
            if (kotlin.math.abs(scale.z) > 1e-6f) 1f / scale.z else 0f
        )
        val result = identity()
        result.elements[0] = elements[0] * invScale.x
        result.elements[1] = elements[1] * invScale.x
        result.elements[2] = elements[2] * invScale.x
        result.elements[4] = elements[4] * invScale.y
        result.elements[5] = elements[5] * invScale.y
        result.elements[6] = elements[6] * invScale.y
        result.elements[8] = elements[8] * invScale.z
        result.elements[9] = elements[9] * invScale.z
        result.elements[10] = elements[10] * invScale.z
        result.elements[12] = 0f
        result.elements[13] = 0f
        result.elements[14] = 0f
        return result
    }

    /**
     * Multiplies this matrix by [other] and returns the result.
     */
    operator fun times(other: Mat4): Mat4 {
        val result = Mat4(FloatArray(16))
        for (col in 0..3) {
            for (row in 0..3) {
                var sum = 0f
                for (k in 0..3) {
                    sum += this[row, k] * other[k, col]
                }
                result[row, col] = sum
            }
        }
        return result
    }

    /**
     * Transforms a [Vec4] by this matrix (matrix-vector multiplication).
     */
    fun transform(v: Vec4): Vec4 = Vec4(
        elements[0] * v.x + elements[4] * v.y + elements[8] * v.z + elements[12] * v.w,
        elements[1] * v.x + elements[5] * v.y + elements[9] * v.z + elements[13] * v.w,
        elements[2] * v.x + elements[6] * v.y + elements[10] * v.z + elements[14] * v.w,
        elements[3] * v.x + elements[7] * v.y + elements[11] * v.z + elements[15] * v.w
    )

    /**
     * Transforms a point (Vec3) by this matrix. The point is treated as a homogeneous
     * coordinate with w=1, and the result is perspective-divided.
     */
    fun transformPoint(v: Vec3): Vec3 {
        val wx = elements[3] * v.x + elements[7] * v.y + elements[11] * v.z + elements[15]
        val invW = if (kotlin.math.abs(wx) > 1e-6f) 1f / wx else 1f
        return Vec3(
            (elements[0] * v.x + elements[4] * v.y + elements[8] * v.z + elements[12]) * invW,
            (elements[1] * v.x + elements[5] * v.y + elements[9] * v.z + elements[13]) * invW,
            (elements[2] * v.x + elements[6] * v.y + elements[10] * v.z + elements[14]) * invW
        )
    }

    /**
     * Transforms a direction (Vec3) by this matrix. The direction is treated as a
     * homogeneous coordinate with w=0 (no translation applied).
     */
    fun transformDirection(v: Vec3): Vec3 = Vec3(
        elements[0] * v.x + elements[4] * v.y + elements[8] * v.z,
        elements[1] * v.x + elements[5] * v.y + elements[9] * v.z,
        elements[2] * v.x + elements[6] * v.y + elements[10] * v.z
    )

    /**
     * Returns the transpose of this matrix.
     */
    fun transpose(): Mat4 {
        val result = Mat4(FloatArray(16))
        for (col in 0..3) {
            for (row in 0..3) {
                result[row, col] = this[col, row]
            }
        }
        return result
    }

    /**
     * Returns the determinant of this 4x4 matrix.
     */
    fun determinant(): Float {
        val m = elements
        return m[12] * m[9] * m[6] * m[3] -
                m[8] * m[13] * m[6] * m[3] -
                m[12] * m[5] * m[10] * m[3] +
                m[4] * m[13] * m[10] * m[3] +
                m[8] * m[5] * m[14] * m[3] -
                m[4] * m[9] * m[14] * m[3] -
                m[12] * m[9] * m[2] * m[7] +
                m[8] * m[13] * m[2] * m[7] +
                m[12] * m[1] * m[10] * m[7] -
                m[0] * m[13] * m[10] * m[7] -
                m[8] * m[1] * m[14] * m[7] +
                m[0] * m[9] * m[14] * m[7] +
                m[12] * m[5] * m[2] * m[11] -
                m[4] * m[13] * m[2] * m[11] -
                m[12] * m[1] * m[6] * m[11] +
                m[0] * m[13] * m[6] * m[11] +
                m[4] * m[1] * m[14] * m[11] -
                m[0] * m[5] * m[14] * m[11] -
                m[8] * m[5] * m[2] * m[15] +
                m[4] * m[9] * m[2] * m[15] +
                m[8] * m[1] * m[6] * m[15] -
                m[0] * m[9] * m[6] * m[15] -
                m[4] * m[1] * m[10] * m[15] +
                m[0] * m[5] * m[10] * m[15]
    }

    /**
     * Returns the inverse of this matrix, or throws [ArithmeticException] if the matrix is singular.
     */
    fun inverse(): Mat4 {
        val m = elements
        val det = determinant()
        if (kotlin.math.abs(det) < 1e-10f) {
            throw ArithmeticException("Matrix is singular and cannot be inverted.")
        }
        val invDet = 1f / det
        val result = FloatArray(16)

        result[0] = (m[9] * m[14] * m[7] - m[13] * m[10] * m[7] + m[13] * m[6] * m[11] - m[5] * m[14] * m[11] - m[9] * m[6] * m[15] + m[5] * m[10] * m[15]) * invDet
        result[1] = (m[13] * m[10] * m[3] - m[9] * m[14] * m[3] - m[13] * m[2] * m[11] + m[1] * m[14] * m[11] + m[9] * m[2] * m[15] - m[1] * m[10] * m[15]) * invDet
        result[2] = (m[5] * m[14] * m[3] - m[13] * m[6] * m[3] + m[13] * m[2] * m[7] - m[1] * m[14] * m[7] - m[5] * m[2] * m[15] + m[1] * m[6] * m[15]) * invDet
        result[3] = (m[9] * m[6] * m[3] - m[5] * m[10] * m[3] - m[9] * m[2] * m[7] + m[1] * m[10] * m[7] + m[5] * m[2] * m[11] - m[1] * m[6] * m[11]) * invDet

        result[4] = (m[12] * m[10] * m[7] - m[8] * m[14] * m[7] - m[12] * m[6] * m[11] + m[4] * m[14] * m[11] + m[8] * m[6] * m[15] - m[4] * m[10] * m[15]) * invDet
        result[5] = (m[8] * m[14] * m[3] - m[12] * m[10] * m[3] + m[12] * m[2] * m[11] - m[0] * m[14] * m[11] - m[8] * m[2] * m[15] + m[0] * m[10] * m[15]) * invDet
        result[6] = (m[12] * m[6] * m[3] - m[4] * m[14] * m[3] - m[12] * m[2] * m[7] + m[0] * m[14] * m[7] + m[4] * m[2] * m[15] - m[0] * m[6] * m[15]) * invDet
        result[7] = (m[4] * m[10] * m[3] - m[8] * m[6] * m[3] + m[8] * m[2] * m[7] - m[0] * m[10] * m[7] - m[4] * m[2] * m[11] + m[0] * m[6] * m[11]) * invDet

        result[8] = (m[8] * m[13] * m[7] - m[12] * m[9] * m[7] + m[12] * m[5] * m[11] - m[4] * m[13] * m[11] - m[8] * m[5] * m[15] + m[4] * m[9] * m[15]) * invDet
        result[9] = (m[12] * m[9] * m[3] - m[8] * m[13] * m[3] - m[12] * m[1] * m[11] + m[0] * m[13] * m[11] + m[8] * m[1] * m[15] - m[0] * m[9] * m[15]) * invDet
        result[10] = (m[4] * m[13] * m[3] - m[12] * m[5] * m[3] + m[12] * m[1] * m[7] - m[0] * m[13] * m[7] - m[4] * m[1] * m[15] + m[0] * m[5] * m[15]) * invDet
        result[11] = (m[8] * m[5] * m[3] - m[4] * m[9] * m[3] - m[8] * m[1] * m[7] + m[0] * m[9] * m[7] + m[4] * m[1] * m[11] - m[0] * m[5] * m[11]) * invDet

        result[12] = (m[12] * m[9] * m[6] - m[8] * m[13] * m[6] - m[12] * m[5] * m[10] + m[4] * m[13] * m[10] + m[8] * m[5] * m[14] - m[4] * m[9] * m[14]) * invDet
        result[13] = (m[8] * m[13] * m[2] - m[12] * m[9] * m[2] + m[12] * m[1] * m[10] - m[0] * m[13] * m[10] - m[8] * m[1] * m[14] + m[0] * m[9] * m[14]) * invDet
        result[14] = (m[12] * m[5] * m[2] - m[4] * m[13] * m[2] - m[12] * m[1] * m[6] + m[0] * m[13] * m[6] + m[4] * m[1] * m[14] - m[0] * m[5] * m[14]) * invDet
        result[15] = (m[4] * m[9] * m[2] - m[8] * m[5] * m[2] + m[8] * m[1] * m[6] - m[0] * m[9] * m[6] - m[4] * m[1] * m[10] + m[0] * m[5] * m[10]) * invDet

        return Mat4(result)
    }

    override fun toString(): String {
        return buildString {
            appendLine("Mat4(")
            for (row in 0..3) {
                append("  ")
                for (col in 0..3) {
                    append("%10.4f".format(this@Mat4[row, col]))
                    if (col < 3) append(", ")
                }
                if (row < 3) appendLine(",")
            }
            appendLine(")")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Mat4) return false
        return elements.contentEquals(other.elements)
    }

    override fun hashCode(): Int = elements.contentHashCode()

    companion object {

        /**
         * Creates a 4x4 identity matrix.
         */
        fun identity(): Mat4 = Mat4()

        /**
         * Creates an orthographic projection matrix.
         *
         * @param left   Left edge of the near plane.
         * @param right  Right edge of the near plane.
         * @param bottom Bottom edge of the near plane.
         * @param top    Top edge of the near plane.
         * @param near   Distance to the near clipping plane.
         * @param far    Distance to the far clipping plane.
         */
        fun ortho(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float): Mat4 {
            val elements = FloatArray(16)
            val width = right - left
            val height = top - bottom
            val depth = far - near

            elements[0] = 2f / width
            elements[5] = 2f / height
            elements[10] = -2f / depth
            elements[12] = -(right + left) / width
            elements[13] = -(top + bottom) / height
            elements[14] = -(far + near) / depth
            elements[15] = 1f

            return Mat4(elements)
        }

        /**
         * Creates a perspective projection matrix.
         *
         * @param fov    Vertical field of view in radians.
         * @param aspect Aspect ratio (width / height).
         * @param near   Distance to the near clipping plane (must be positive).
         * @param far    Distance to the far clipping plane (must be positive and greater than near).
         */
        fun perspective(fov: Float, aspect: Float, near: Float, far: Float): Mat4 {
            val elements = FloatArray(16)
            val tanHalfFov = tan(fov / 2f)

            elements[0] = 1f / (aspect * tanHalfFov)
            elements[5] = 1f / tanHalfFov
            elements[10] = -(far + near) / (far - near)
            elements[11] = -1f
            elements[14] = -(2f * far * near) / (far - near)

            return Mat4(elements)
        }

        /**
         * Creates a view matrix that looks from [eye] towards [center] with the given [up] direction.
         *
         * @param eye    The position of the camera.
         * @param center The point the camera is looking at.
         * @param up     The up direction (typically 0, 1, 0).
         */
        fun lookAt(eye: Vec3, center: Vec3, up: Vec3): Mat4 {
            val f = (center - eye).normalized()
            val s = f.cross(up).normalized()
            val u = s.cross(f)

            val elements = FloatArray(16)
            elements[0] = s.x
            elements[1] = u.x
            elements[2] = -f.x
            elements[4] = s.y
            elements[5] = u.y
            elements[6] = -f.y
            elements[8] = s.z
            elements[9] = u.z
            elements[10] = -f.z
            elements[12] = -s.dot(eye)
            elements[13] = -u.dot(eye)
            elements[14] = f.dot(eye)
            elements[15] = 1f

            return Mat4(elements)
        }

        /**
         * Creates a translation matrix.
         */
        fun translation(x: Float, y: Float, z: Float): Mat4 {
            val elements = FloatArray(16)
            elements[0] = 1f
            elements[5] = 1f
            elements[10] = 1f
            elements[12] = x
            elements[13] = y
            elements[14] = z
            elements[15] = 1f
            return Mat4(elements)
        }

        /**
         * Creates a translation matrix from a Vec3.
         */
        fun translation(t: Vec3): Mat4 = translation(t.x, t.y, t.z)

        /**
         * Creates a uniform or non-uniform scale matrix.
         */
        fun scale(x: Float, y: Float, z: Float): Mat4 {
            val elements = FloatArray(16)
            elements[0] = x
            elements[5] = y
            elements[10] = z
            elements[15] = 1f
            return Mat4(elements)
        }

        /**
         * Creates a scale matrix from a Vec3.
         */
        fun scale(s: Vec3): Mat4 = scale(s.x, s.y, s.z)

        /**
         * Creates a rotation matrix around an arbitrary axis.
         *
         * @param angle Rotation angle in radians.
         * @param axis  The axis of rotation (will be normalized).
         */
        fun rotation(angle: Float, axis: Vec3): Mat4 {
            val a = axis.normalized()
            val cosA = cos(angle)
            val sinA = sin(angle)
            val oneMinusCos = 1f - cosA

            val elements = FloatArray(16)
            elements[0] = a.x * a.x * oneMinusCos + cosA
            elements[1] = a.x * a.y * oneMinusCos + a.z * sinA
            elements[2] = a.x * a.z * oneMinusCos - a.y * sinA

            elements[4] = a.y * a.x * oneMinusCos - a.z * sinA
            elements[5] = a.y * a.y * oneMinusCos + cosA
            elements[6] = a.y * a.z * oneMinusCos + a.x * sinA

            elements[8] = a.z * a.x * oneMinusCos + a.y * sinA
            elements[9] = a.z * a.y * oneMinusCos - a.x * sinA
            elements[10] = a.z * a.z * oneMinusCos + cosA

            elements[15] = 1f

            return Mat4(elements)
        }
    }
}
