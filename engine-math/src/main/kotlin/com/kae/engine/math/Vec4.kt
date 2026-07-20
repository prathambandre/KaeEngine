package com.kae.engine.math

import kotlin.math.sqrt

/**
 * A 4D vector represented by [x], [y], [z], and [w] components.
 */
data class Vec4(val x: Float = 0f, val y: Float = 0f, val z: Float = 0f, val w: Float = 0f) {

    /** Returns the negation of this vector. */
    operator fun unaryMinus(): Vec4 = Vec4(-x + 0f, -y + 0f, -z + 0f, -w + 0f)

    /** Adds another vector to this vector. */
    operator fun plus(other: Vec4): Vec4 = Vec4(x + other.x, y + other.y, z + other.z, w + other.w)

    /** Subtracts another vector from this vector. */
    operator fun minus(other: Vec4): Vec4 = Vec4(x - other.x, y - other.y, z - other.z, w - other.w)

    /** Scales this vector by a scalar value. */
    operator fun times(scalar: Float): Vec4 = Vec4(x * scalar, y * scalar, z * scalar, w * scalar)

    /** Scales this vector by a scalar value (scalar on the left). */
    operator fun Float.times(v: Vec4): Vec4 = Vec4(v.x * this, v.y * this, v.z * this, v.w * this)

    /** Component-wise multiplication with another vector. */
    operator fun times(other: Vec4): Vec4 = Vec4(x * other.x, y * other.y, z * other.z, w * other.w)

    /** Divides this vector by a scalar value. */
    operator fun div(scalar: Float): Vec4 = Vec4(x / scalar, y / scalar, z / scalar, w / scalar)

    /** Component-wise division with another vector. */
    operator fun div(other: Vec4): Vec4 = Vec4(x / other.x, y / other.y, z / other.z, w / other.w)

    /** Computes the dot product of this vector and [other]. */
    fun dot(other: Vec4): Float = x * other.x + y * other.y + z * other.z + w * other.w

    /** Returns the magnitude (length) of this vector. */
    fun length(): Float = sqrt(x * x + y * y + z * z + w * w)

    /** Returns the squared magnitude of this vector. */
    fun lengthSquared(): Float = x * x + y * y + z * z + w * w

    /** Returns a unit vector in the same direction as this vector. */
    fun normalized(): Vec4 {
        val len = length()
        return if (len > 0f) Vec4(x / len, y / len, z / len, w / len) else Vec4(0f, 0f, 0f, 0f)
    }

    /** Returns a new normalized copy of this vector. */
    fun normalize(): Vec4 = normalized()

    /** Returns the distance from this vector to [other]. */
    fun distanceTo(other: Vec4): Float = (this - other).length()

    /** Returns the squared distance from this vector to [other]. */
    fun distanceSquaredTo(other: Vec4): Float = (this - other).lengthSquared()

    /** Linearly interpolates between this vector and [other] by factor [t]. */
    fun lerp(other: Vec4, t: Float): Vec4 =
        Vec4(x + (other.x - x) * t, y + (other.y - y) * t, z + (other.z - z) * t, w + (other.w - w) * t)

    /** Returns the Vec3 formed by the x, y, and z components. */
    fun xyz(): Vec3 = Vec3(x, y, z)

    /** Returns the Vec2 formed by the x and y components. */
    fun xy(): Vec2 = Vec2(x, y)

    /** Returns the Vec2 formed by the x and w components. */
    fun xw(): Vec2 = Vec2(x, w)

    /** Returns the Vec2 formed by the y and z components. */
    fun yz(): Vec2 = Vec2(y, z)

    /** Returns the Vec2 formed by the y and w components. */
    fun yw(): Vec2 = Vec2(y, w)

    /** Returns the Vec2 formed by the z and w components. */
    fun zw(): Vec2 = Vec2(z, w)

    /** Returns the Vec3 formed by the x, y, and w components. */
    fun xyw(): Vec3 = Vec3(x, y, w)

    /** Returns the Vec3 formed by the x, z, and w components. */
    fun xzw(): Vec3 = Vec3(x, z, w)

    /** Returns the Vec3 formed by the y, z, and w components. */
    fun yzw(): Vec3 = Vec3(y, z, w)

    override fun toString(): String = "Vec4($x, $y, $z, $w)"

    companion object {
        /** The zero vector (0, 0, 0, 0). */
        val ZERO = Vec4(0f, 0f, 0f, 0f)

        /** The one vector (1, 1, 1, 1). */
        val ONE = Vec4(1f, 1f, 1f, 1f)

        /** A unit vector along the x-axis (1, 0, 0, 0). */
        val UNIT_X = Vec4(1f, 0f, 0f, 0f)

        /** A unit vector along the y-axis (0, 1, 0, 0). */
        val UNIT_Y = Vec4(0f, 1f, 0f, 0f)

        /** A unit vector along the z-axis (0, 0, 1, 0). */
        val UNIT_Z = Vec4(0f, 0f, 1f, 0f)

        /** A unit vector along the w-axis (0, 0, 0, 1). */
        val UNIT_W = Vec4(0f, 0f, 0f, 1f)
    }
}
