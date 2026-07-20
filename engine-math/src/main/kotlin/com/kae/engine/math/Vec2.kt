package com.kae.engine.math

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sqrt
import kotlin.math.sin

/**
 * A 2D vector represented by [x] and [y] components.
 */
data class Vec2(val x: Float = 0f, val y: Float = 0f) {

    /** Returns the negation of this vector. */
    operator fun unaryMinus(): Vec2 = Vec2(-x + 0f, -y + 0f)

    /** Adds another vector to this vector. */
    operator fun plus(other: Vec2): Vec2 = Vec2(x + other.x, y + other.y)

    /** Subtracts another vector from this vector. */
    operator fun minus(other: Vec2): Vec2 = Vec2(x - other.x, y - other.y)

    /** Scales this vector by a scalar value. */
    operator fun times(scalar: Float): Vec2 = Vec2(x * scalar, y * scalar)

    /** Component-wise multiplication with another vector. */
    operator fun times(other: Vec2): Vec2 = Vec2(x * other.x, y * other.y)

    /** Divides this vector by a scalar value. */
    operator fun div(scalar: Float): Vec2 = Vec2(x / scalar, y / scalar)

    /** Component-wise division with another vector. */
    operator fun div(other: Vec2): Vec2 = Vec2(x / other.x, y / other.y)

    /** Computes the dot product of this vector and [other]. */
    fun dot(other: Vec2): Float = x * other.x + y * other.y

    /**
     * Computes the 2D cross product (z-component of the 3D cross product).
     * Returns a scalar representing the magnitude of the perpendicular vector.
     */
    fun cross(other: Vec2): Float = x * other.y - y * other.x

    /** Returns the magnitude (length) of this vector. */
    fun length(): Float = sqrt(x * x + y * y)

    /** Returns the squared magnitude of this vector. Faster than [length] when only comparisons are needed. */
    fun lengthSquared(): Float = x * x + y * y

    /** Returns a unit vector in the same direction as this vector. */
    fun normalized(): Vec2 {
        val len = length()
        return if (len > 0f) Vec2(x / len, y / len) else Vec2(0f, 0f)
    }

    /** Normalizes this vector in place (no-op on data class, returns new normalized copy). */
    fun normalize(): Vec2 = normalized()

    /** Returns the distance from this vector to [other]. */
    fun distanceTo(other: Vec2): Float = (this - other).length()

    /** Returns the squared distance from this vector to [other]. */
    fun distanceSquaredTo(other: Vec2): Float = (this - other).lengthSquared()

    /** Linearly interpolates between this vector and [other] by factor [t]. */
    fun lerp(other: Vec2, t: Float): Vec2 =
        Vec2(x + (other.x - x) * t, y + (other.y - y) * t)

    /**
     * Reflects this vector off a surface with the given [normal].
     * The normal must be a unit vector.
     */
    fun reflect(normal: Vec2): Vec2 = this - normal * (2f * dot(normal))

    /** Returns the angle of this vector in radians relative to the positive x-axis. */
    fun angle(): Float = atan2(y, x)

    /**
     * Rotates this vector by [radians] radians counter-clockwise around the origin.
     */
    fun rotate(radians: Float): Vec2 {
        val cosA = cos(radians)
        val sinA = sin(radians)
        return Vec2(x * cosA - y * sinA, x * sinA + y * cosA)
    }

    /** Returns a vector perpendicular to this vector (rotated 90 degrees counter-clockwise). */
    fun perpendicular(): Vec2 = Vec2(-y, x)

    override fun toString(): String = "Vec2($x, $y)"

    companion object {
        /** The zero vector (0, 0). */
        val ZERO = Vec2(0f, 0f)

        /** The one vector (1, 1). */
        val ONE = Vec2(1f, 1f)

        /** Unit vector pointing up (0, 1). */
        val UP = Vec2(0f, 1f)

        /** Unit vector pointing down (0, -1). */
        val DOWN = Vec2(0f, -1f)

        /** Unit vector pointing left (-1, 0). */
        val LEFT = Vec2(-1f, 0f)

        /** Unit vector pointing right (1, 0). */
        val RIGHT = Vec2(1f, 0f)
    }
}

/** Scales a vector by a scalar value (scalar on the left). */
operator fun Float.times(v: Vec2): Vec2 = Vec2(v.x * this, v.y * this)
