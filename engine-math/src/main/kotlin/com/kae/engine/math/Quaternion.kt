package com.kae.engine.math

import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * A quaternion representing a 3D rotation.
 *
 * Stored as (x, y, z, w) where w is the scalar component.
 * Quaternion multiplication is performed as: q * r = (q.w * r.v + r.w * q.v + q.v × r.v, q.w * r.w − q.v · r.v).
 */
data class Quaternion(val x: Float = 0f, val y: Float = 0f, val z: Float = 0f, val w: Float = 1f) {

    /**
     * Returns the magnitude (length) of this quaternion.
     */
    fun length(): Float = sqrt(x * x + y * y + z * z + w * w)

    /**
     * Returns the squared magnitude of this quaternion.
     */
    fun lengthSquared(): Float = x * x + y * y + z * z + w * w

    /**
     * Returns a unit quaternion in the same direction as this quaternion.
     */
    fun normalized(): Quaternion {
        val len = length()
        return if (len > 0f) Quaternion(x / len, y / len, z / len, w / len) else IDENTITY
    }

    /**
     * Returns a new normalized copy of this quaternion.
     */
    fun normalize(): Quaternion = normalized()

    /**
     * Returns the conjugate of this quaternion (negates the imaginary components).
     * For unit quaternions, this is equivalent to the inverse.
     */
    fun conjugate(): Quaternion = Quaternion(-x, -y, -z, w)

    /**
     * Returns the inverse of this quaternion.
     */
    fun inverse(): Quaternion {
        val lenSq = lengthSquared()
        return if (lenSq > 1e-10f) {
            Quaternion(-x / lenSq, -y / lenSq, -z / lenSq, w / lenSq)
        } else {
            IDENTITY
        }
    }

    /**
     * Computes the dot product of this quaternion and [other].
     */
    fun dot(other: Quaternion): Float = x * other.x + y * other.y + z * other.z + w * other.w

    /**
     * Multiplies this quaternion by [other] (applies rotation [other] then this).
     * Quaternion multiplication is not commutative.
     */
    operator fun times(other: Quaternion): Quaternion = Quaternion(
        w * other.x + x * other.w + y * other.z - z * other.y,
        w * other.y - x * other.z + y * other.w + z * other.x,
        w * other.z + x * other.y - y * other.x + z * other.w,
        w * other.w - x * other.x - y * other.y - z * other.z
    )

    /**
     * Rotates the vector [v] by this quaternion.
     * Equivalent to (q * v * q⁻¹) where v is treated as a pure quaternion (0, vx, vy, vz).
     */
    operator fun times(v: Vec3): Vec3 {
        val qv = Vec3(x, y, z)
        val uv = qv.cross(v)
        val uuv = qv.cross(uv)
        return v + (uv * w + uuv) * 2f
    }

    /**
     * Converts this quaternion to a 4x4 rotation matrix.
     */
    fun toMat4(): Mat4 {
        val elements = FloatArray(16)
        val xx = x * x
        val yy = y * y
        val zz = z * z
        val xy = x * y
        val xz = x * z
        val yz = y * z
        val wx = w * x
        val wy = w * y
        val wz = w * z

        elements[0] = 1f - 2f * (yy + zz)
        elements[1] = 2f * (xy + wz)
        elements[2] = 2f * (xz - wy)

        elements[4] = 2f * (xy - wz)
        elements[5] = 1f - 2f * (xx + zz)
        elements[6] = 2f * (yz + wx)

        elements[8] = 2f * (xz + wy)
        elements[9] = 2f * (yz - wx)
        elements[10] = 1f - 2f * (xx + yy)

        elements[12] = 0f
        elements[13] = 0f
        elements[14] = 0f
        elements[15] = 1f

        return Mat4(elements)
    }

    /**
     * Converts this quaternion to Euler angles (pitch, yaw, roll) in radians.
     * - Pitch: rotation around X axis.
     * - Yaw:   rotation around Y axis.
     * - Roll:  rotation around Z axis.
     */
    fun toEuler(): Vec3 {
        val sinr_cosp = 2f * (w * x + y * z)
        val cosr_cosp = 1f - 2f * (x * x + y * y)
        val pitch = atan2(sinr_cosp, cosr_cosp)

        val sinp = 2f * (w * y - z * x)
        val yaw = if (kotlin.math.abs(sinp) >= 1f) {
            kotlin.math.sign(sinp) * (Math.PI.toFloat() / 2f)
        } else {
            asin(sinp)
        }

        val siny_cosp = 2f * (w * z + x * y)
        val cosy_cosp = 1f - 2f * (y * y + z * z)
        val roll = atan2(siny_cosp, cosy_cosp)

        return Vec3(pitch, yaw, roll)
    }

    /**
     * Returns the angle in radians between this quaternion and [other].
     */
    fun angleBetween(other: Quaternion): Float {
        val d = kotlin.math.abs(dot(other))
        val clamped = if (d > 1f) 1f else d
        return 2f * acos(clamped)
    }

    /**
     * Normalized linear interpolation between this quaternion and [other] by factor [t].
     */
    fun nlerp(other: Quaternion, t: Float): Quaternion {
        val dot = this.dot(other)
        val target = if (dot < 0f) -other else other
        return (this * (1f - t) + target * t).normalized()
    }

    private fun asin(x: Float): Float = kotlin.math.asin(x)
    private fun atan2(y: Float, x: Float): Float = kotlin.math.atan2(y, x)

    operator fun unaryMinus(): Quaternion = Quaternion(-x + 0f, -y + 0f, -z + 0f, -w + 0f)

    operator fun times(scalar: Float): Quaternion = Quaternion(x * scalar, y * scalar, z * scalar, w * scalar)

    operator fun plus(other: Quaternion): Quaternion = Quaternion(x + other.x, y + other.y, z + other.z, w + other.w)

    operator fun minus(other: Quaternion): Quaternion = Quaternion(x - other.x, y - other.y, z - other.z, w - other.w)

    override fun toString(): String = "Quaternion($x, $y, $z, $w)"

    companion object {

        /** The identity quaternion (no rotation). */
        val IDENTITY = Quaternion(0f, 0f, 0f, 1f)

        /**
         * Creates a quaternion from an axis and angle of rotation.
         *
         * @param axis    The axis of rotation (will be normalized).
         * @param radians The rotation angle in radians.
         */
        fun fromAxisAngle(axis: Vec3, radians: Float): Quaternion {
            val a = axis.normalized()
            val halfAngle = radians / 2f
            val s = sin(halfAngle)
            return Quaternion(a.x * s, a.y * s, a.z * s, cos(halfAngle))
        }

        /**
         * Creates a quaternion from Euler angles (pitch, yaw, roll) in radians.
         * Rotation order: YXZ (yaw * pitch * roll).
         *
         * @param pitch Rotation around the X axis in radians.
         * @param yaw   Rotation around the Y axis in radians.
         * @param roll  Rotation around the Z axis in radians.
         */
        fun fromEuler(pitch: Float, yaw: Float, roll: Float): Quaternion {
            val halfPitch = pitch / 2f
            val halfYaw = yaw / 2f
            val halfRoll = roll / 2f

            val cp = cos(halfPitch)
            val sp = sin(halfPitch)
            val cy = cos(halfYaw)
            val sy = sin(halfYaw)
            val cr = cos(halfRoll)
            val sr = sin(halfRoll)

            return Quaternion(
                x = cy * sp * cr + sy * cp * sr,
                y = sy * cp * cr - cy * sp * sr,
                z = cy * cp * sr - sy * sp * cr,
                w = cy * cp * cr + sy * sp * sr
            )
        }

        /**
         * Spherical linear interpolation between two quaternions [a] and [b] by factor [t].
         * [t] is in the range 0..1.
         */
        fun slerp(a: Quaternion, b: Quaternion, t: Float): Quaternion {
            if (t <= 0f) return a
            if (t >= 1f) return b

            var dot = a.dot(b)
            var bAdj = b

            // If the dot product is negative, negate one quaternion to take the shorter path.
            if (dot < 0f) {
                bAdj = Quaternion(-b.x + 0f, -b.y + 0f, -b.z + 0f, -b.w + 0f)
                dot = -dot
            }

            // If the quaternions are very close, use linear interpolation to avoid division by zero.
            if (dot > 0.9995f) {
                return Quaternion(
                    a.x + (bAdj.x - a.x) * t,
                    a.y + (bAdj.y - a.y) * t,
                    a.z + (bAdj.z - a.z) * t,
                    a.w + (bAdj.w - a.w) * t
                ).normalized()
            }

            val theta0 = acos(dot.coerceIn(-1f, 1f))
            val theta = theta0 * t
            val sinTheta = sin(theta)
            val sinTheta0 = sin(theta0)

            val wa = cos(theta) - dot * sinTheta / sinTheta0
            val wb = sinTheta / sinTheta0

            return Quaternion(
                a.x * wa + bAdj.x * wb,
                a.y * wa + bAdj.y * wb,
                a.z * wa + bAdj.z * wb,
                a.w * wa + bAdj.w * wb
            )
        }
    }
}
