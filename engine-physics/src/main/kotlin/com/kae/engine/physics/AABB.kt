package com.kae.engine.physics

import com.kae.engine.math.Vec2
import kotlin.math.max
import kotlin.math.min

/**
 * Axis-Aligned Bounding Box defined by minimum and maximum corners.
 */
data class AABB(val min: Vec2, val max: Vec2) {

    val center: Vec2 get() = (min + max) * 0.5f

    val size: Vec2 get() = max - min

    val halfSize: Vec2 get() = size * 0.5f

    /**
     * Returns true if this AABB overlaps with [other].
     */
    fun intersects(other: AABB): Boolean {
        if (max.x < other.min.x || min.x > other.max.x) return false
        if (max.y < other.min.y || min.y > other.max.y) return false
        return true
    }

    /**
     * Returns true if this AABB fully contains [other].
     */
    fun contains(other: AABB): Boolean {
        return min.x <= other.min.x && min.y <= other.min.y &&
                max.x >= other.max.x && max.y >= other.max.y
    }

    /**
     * Returns true if this AABB contains the given [point].
     */
    fun containsPoint(point: Vec2): Boolean {
        return point.x >= min.x && point.x <= max.x &&
                point.y >= min.y && point.y <= max.y
    }

    /**
     * Returns the smallest AABB that contains both this and [other].
     */
    fun merge(other: AABB): AABB {
        return AABB(
            min = Vec2(kotlin.math.min(min.x, other.min.x), kotlin.math.min(min.y, other.min.y)),
            max = Vec2(kotlin.math.max(max.x, other.max.x), kotlin.math.max(max.y, other.max.y))
        )
    }

    /**
     * Returns a new AABB expanded to include the given [point].
     */
    fun expand(point: Vec2): AABB {
        return AABB(
            min = Vec2(kotlin.math.min(min.x, point.x), kotlin.math.min(min.y, point.y)),
            max = Vec2(kotlin.math.max(max.x, point.x), kotlin.math.max(max.y, point.y))
        )
    }

    /**
     * Returns the area of this AABB.
     */
    fun area(): Float {
        val s = size
        return s.x * s.y
    }

    /**
     * Returns the perimeter of this AABB.
     */
    fun perimeter(): Float {
        val s = size
        return 2f * (s.x + s.y)
    }

    /**
     * Returns the intersection of this AABB with [other], or null if they don't overlap.
     */
    fun overlap(other: AABB): AABB? {
        val newMinX = max(min.x, other.min.x)
        val newMinY = max(min.y, other.min.y)
        val newMaxX = min(max.x, other.max.x)
        val newMaxY = min(max.y, other.max.y)
        if (newMinX > newMaxX || newMinY > newMaxY) return null
        return AABB(Vec2(newMinX, newMinY), Vec2(newMaxX, newMaxY))
    }

    companion object {
        /**
         * Creates an AABB from a center point and half-extents.
         */
        fun fromCenterHalf(center: Vec2, halfExtents: Vec2): AABB {
            return AABB(center - halfExtents, center + halfExtents)
        }

        /**
         * Creates an AABB that encompasses all given points.
         */
        fun fromPoints(points: List<Vec2>): AABB? {
            if (points.isEmpty()) return null
            var minX = Float.MAX_VALUE
            var minY = Float.MAX_VALUE
            var maxX = Float.MIN_VALUE
            var maxY = Float.MIN_VALUE
            for (p in points) {
                if (p.x < minX) minX = p.x
                if (p.y < minY) minY = p.y
                if (p.x > maxX) maxX = p.x
                if (p.y > maxY) maxY = p.y
            }
            return AABB(Vec2(minX, minY), Vec2(maxX, maxY))
        }
    }
}
