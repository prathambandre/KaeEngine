package com.kae.engine.math

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sqrt
import kotlin.math.sin

/**
 * A 3D vector represented by [x], [y], and [z] components.
 */
data class Vec3(val x: Float = 0f, val y: Float = 0f, val z: Float = 0f) {

    /** Returns the negation of this vector. */
    operator fun unaryMinus(): Vec3 = Vec3(-x + 0f, -y + 0f, -z + 0f)

    /** Adds another vector to this vector. */
    operator fun plus(other: Vec3): Vec3 = Vec3(x + other.x, y + other.y, z + other.z)

    /** Subtracts another vector from this vector. */
    operator fun minus(other: Vec3): Vec3 = Vec3(x - other.x, y - other.y, z - other.z)

    /** Scales this vector by a scalar value. */
    operator fun times(scalar: Float): Vec3 = Vec3(x * scalar, y * scalar, z * scalar)

    /** Component-wise multiplication with another vector. */
    operator fun times(other: Vec3): Vec3 = Vec3(x * other.x, y * other.y, z * other.z)

    /** Divides this vector by a scalar value. */
    operator fun div(scalar: Float): Vec3 = Vec3(x / scalar, y / scalar, z / scalar)

    /** Component-wise division with another vector. */
    operator fun div(other: Vec3): Vec3 = Vec3(x / other.x, y / other.y, z / other.z)

    /** Computes the dot product of this vector and [other]. */
    fun dot(other: Vec3): Float = x * other.x + y * other.y + z * other.z

    /**
     * Computes the cross product of this vector and [other].
     * The result is a vector perpendicular to both input vectors.
     */
    fun cross(other: Vec3): Vec3 = Vec3(
        y * other.z - z * other.y,
        z * other.x - x * other.z,
        x * other.y - y * other.x
    )

    /** Returns the magnitude (length) of this vector. */
    fun length(): Float = sqrt(x * x + y * y + z * z)

    /** Returns the squared magnitude of this vector. Faster than [length] when only comparisons are needed. */
    fun lengthSquared(): Float = x * x + y * y + z * z

    /** Returns a unit vector in the same direction as this vector. */
    fun normalized(): Vec3 {
        val len = length()
        return if (len > 0f) Vec3(x / len, y / len, z / len) else Vec3(0f, 0f, 0f)
    }

    /** Returns a new normalized copy of this vector. */
    fun normalize(): Vec3 = normalized()

    /** Returns the distance from this vector to [other]. */
    fun distanceTo(other: Vec3): Float = (this - other).length()

    /** Returns the squared distance from this vector to [other]. */
    fun distanceSquaredTo(other: Vec3): Float = (this - other).lengthSquared()

    /** Linearly interpolates between this vector and [other] by factor [t]. */
    fun lerp(other: Vec3, t: Float): Vec3 =
        Vec3(x + (other.x - x) * t, y + (other.y - y) * t, z + (other.z - z) * t)

    /**
     * Reflects this vector off a surface with the given [normal].
     * The normal must be a unit vector.
     */
    fun reflect(normal: Vec3): Vec3 = this - normal * (2f * dot(normal))

    /**
     * Computes the angle between this vector and [other] in radians.
     */
    fun angleBetween(other: Vec3): Float {
        val d = dot(other) / (length() * other.length())
        return acos(d.coerceIn(-1f, 1f))
    }

    private fun acos(x: Float): Float = kotlin.math.acos(x)

    /** Returns the Vec2 formed by the x and y components. */
    fun xy(): Vec2 = Vec2(x, y)

    /** Returns the Vec2 formed by the x and z components. */
    fun xz(): Vec2 = Vec2(x, z)

    /** Returns the Vec2 formed by the y and z components. */
    fun yz(): Vec2 = Vec2(y, z)

    override fun toString(): String = "Vec3($x, $y, $z)"

    companion object {
        /** The zero vector (0, 0, 0). */
        val ZERO = Vec3(0f, 0f, 0f)

        /** The one vector (1, 1, 1). */
        val ONE = Vec3(1f, 1f, 1f)

        /** Unit vector pointing up along the y-axis (0, 1, 0). */
        val UP = Vec3(0f, 1f, 0f)

        /** Unit vector pointing down along the negative y-axis (0, -1, 0). */
        val DOWN = Vec3(0f, -1f, 0f)

        /** Unit vector pointing left along the negative x-axis (-1, 0, 0). */
        val LEFT = Vec3(-1f, 0f, 0f)

        /** Unit vector pointing right along the x-axis (1, 0, 0). */
        val RIGHT = Vec3(1f, 0f, 0f)

        /** Unit vector pointing forward along the negative z-axis (0, 0, -1). */
        val FORWARD = Vec3(0f, 0f, -1f)

        /** Unit vector pointing backward along the z-axis (0, 0, 1). */
        val BACK = Vec3(0f, 0f, 1f)

        /** Unit vector pointing up along the z-axis (0, 0, 1). Useful for Z-up coordinate systems. */
        val Z_UP = Vec3(0f, 0f, 1f)
    }
}

/** Scales a vector by a scalar value (scalar on the left). */
operator fun Float.times(v: Vec3): Vec3 = Vec3(v.x * this, v.y * this, v.z * this)
