package com.kae.engine.math

import kotlin.math.ceil
import kotlin.math.floor

/**
 * Common math constants and utility functions used throughout the engine.
 */
object MathUtils {

    /** The mathematical constant PI (π). */
    const val PI: Float = 3.14159265358979323846f

    /** The mathematical constant TAU (2π). */
    const val TAU: Float = PI * 2f

    /** Conversion factor from degrees to radians. */
    const val DEG_TO_RAD: Float = PI / 180f

    /** Conversion factor from radians to degrees. */
    const val RAD_TO_DEG: Float = 180f / PI

    /**
     * Clamps [value] to the range [min]..[max].
     */
    fun clamp(value: Float, min: Float, max: Float): Float {
        if (value < min) return min
        if (value > max) return max
        return value
    }

    /**
     * Clamps [value] to the range [min]..[max] for integers.
     */
    fun clamp(value: Int, min: Int, max: Int): Int {
        if (value < min) return min
        if (value > max) return max
        return value
    }

    /**
     * Linearly interpolates between [a] and [b] by factor [t].
     * [t] is typically in the range 0..1, but is not clamped.
     */
    fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

    /**
     * Returns the parameter [t] such that [lerp](a, b, t) == [value].
     * Inverse of [lerp].
     */
    fun inverseLerp(a: Float, b: Float, value: Float): Float {
        val denom = b - a
        return if (kotlin.math.abs(denom) < 1e-10f) 0f else (value - a) / denom
    }

    /**
     * Remaps [value] from the range [fromMin]..[fromMax] to [toMin]..[toMax].
     */
    fun remap(value: Float, fromMin: Float, fromMax: Float, toMin: Float, toMax: Float): Float {
        val t = inverseLerp(fromMin, fromMax, value)
        return lerp(toMin, toMax, t)
    }

    /**
     * Returns true if [n] is a power of two.
     */
    fun isPowerOfTwo(n: Int): Boolean = n > 0 && (n and (n - 1)) == 0

    /**
     * Returns the smallest power of two that is greater than or equal to [n].
     */
    fun nextPowerOfTwo(n: Int): Int {
        var v = n - 1
        v = v or (v shr 1)
        v = v or (v shr 2)
        v = v or (v shr 4)
        v = v or (v shr 8)
        v = v or (v shr 16)
        return v + 1
    }

    /**
     * Fast floor using integer truncation.
     * For positive values this is equivalent to [kotlin.math.floor].
     * For negative values it correctly rounds down.
     */
    fun fastFloor(x: Float): Int {
        val xi = x.toInt()
        return if (x < xi) xi - 1 else xi
    }

    /**
     * Fast ceiling using integer truncation.
     * For positive values this is equivalent to [kotlin.math.ceil].
     * For negative values it correctly rounds up.
     */
    fun fastCeil(x: Float): Int {
        val xi = x.toInt()
        return if (x > xi) xi + 1 else xi
    }

    /**
     * Performs Hermite interpolation between 0 and 1 when [edge0] < [x] < [edge1].
     * Returns 0 when [x] <= [edge0], and 1 when [x] >= [edge1].
     */
    fun smoothstep(edge0: Float, edge1: Float, x: Float): Float {
        val t = clamp((x - edge0) / (edge1 - edge0), 0f, 1f)
        return t * t * (3f - 2f * t)
    }

    /**
     * Performs Ken Perlin's improved smootherstep between 0 and 1 when [edge0] < [x] < [edge1].
     * Returns 0 when [x] <= [edge0], and 1 when [x] >= [edge1].
     * The first and second derivatives are zero at the endpoints.
     */
    fun smootherstep(edge0: Float, edge1: Float, x: Float): Float {
        val t = clamp((x - edge0) / (edge1 - edge0), 0f, 1f)
        return t * t * t * (t * (t * 6f - 15f) + 10f)
    }
}
