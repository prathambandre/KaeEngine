package com.kae.engine.physics

import com.kae.engine.scene.Component
import com.kae.engine.math.Vec2
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

abstract class Collider2D : Component {
    abstract fun getBounds(position: Vec2, rotation: Float): AABB
    abstract fun containsPoint(position: Vec2, rotation: Float, point: Vec2): Boolean
    abstract fun getVertices(position: Vec2, rotation: Float): List<Vec2>
}

data class BoxCollider2D(
    var halfExtents: Vec2 = Vec2(0.5f, 0.5f),
    var offset: Vec2 = Vec2(0f, 0f)
) : Collider2D() {

    override fun getBounds(position: Vec2, rotation: Float): AABB {
        val absRotX = abs(cos(rotation.toDouble())).toFloat()
        val absRotY = abs(sin(rotation.toDouble())).toFloat()
        val rx = halfExtents.x * absRotX + halfExtents.y * absRotY
        val ry = halfExtents.x * absRotY + halfExtents.y * absRotX
        val pos = position + offset
        return AABB(pos - Vec2(rx, ry), pos + Vec2(rx, ry))
    }

    override fun containsPoint(position: Vec2, rotation: Float, point: Vec2): Boolean {
        val worldPos = position + offset
        val localPoint = point - worldPos
        val rotated = localPoint.rotate(-rotation)
        return abs(rotated.x) <= halfExtents.x && abs(rotated.y) <= halfExtents.y
    }

    override fun getVertices(position: Vec2, rotation: Float): List<Vec2> {
        val worldPos = position + offset
        val corners = listOf(
            Vec2(-halfExtents.x, -halfExtents.y),
            Vec2(halfExtents.x, -halfExtents.y),
            Vec2(halfExtents.x, halfExtents.y),
            Vec2(-halfExtents.x, halfExtents.y)
        )
        return corners.map { it.rotate(rotation) + worldPos }
    }
}

data class CircleCollider2D(
    var radius: Float = 0.5f,
    var offset: Vec2 = Vec2(0f, 0f)
) : Collider2D() {

    override fun getBounds(position: Vec2, rotation: Float): AABB {
        val pos = position + offset
        return AABB(pos - Vec2(radius, radius), pos + Vec2(radius, radius))
    }

    override fun containsPoint(position: Vec2, rotation: Float, point: Vec2): Boolean {
        val worldPos = position + offset
        return worldPos.distanceSquaredTo(point) <= radius * radius
    }

    override fun getVertices(position: Vec2, rotation: Float): List<Vec2> {
        val worldPos = position + offset
        val segments = 16
        return (0 until segments).map { i ->
            val angle = (2.0 * Math.PI * i / segments).toFloat()
            Vec2(worldPos.x + radius * cos(angle), worldPos.y + radius * sin(angle))
        }
    }

    fun closestPoint(position: Vec2, point: Vec2): Vec2 {
        val worldPos = position + offset
        val diff = point - worldPos
        val len = diff.length()
        if (len <= 0f) return worldPos
        return worldPos + diff * (radius / len)
    }
}

data class CapsuleCollider2D(
    var tipA: Vec2 = Vec2(0f, -0.5f),
    var tipB: Vec2 = Vec2(0f, 0.5f),
    var radius: Float = 0.25f,
    var offset: Vec2 = Vec2(0f, 0f)
) : Collider2D() {

    override fun getBounds(position: Vec2, rotation: Float): AABB {
        val worldPos = position + offset
        val a = tipA.rotate(rotation) + worldPos
        val b = tipB.rotate(rotation) + worldPos
        val minX = kotlin.math.min(a.x, b.x) - radius
        val minY = kotlin.math.min(a.y, b.y) - radius
        val maxX = kotlin.math.max(a.x, b.x) + radius
        val maxY = kotlin.math.max(a.y, b.y) + radius
        return AABB(Vec2(minX, minY), Vec2(maxX, maxY))
    }

    override fun containsPoint(position: Vec2, rotation: Float, point: Vec2): Boolean {
        val worldPos = position + offset
        val a = tipA.rotate(rotation) + worldPos
        val b = tipB.rotate(rotation) + worldPos
        val ab = b - a
        val ap = point - a
        val t = (ap.dot(ab) / ab.lengthSquared()).coerceIn(0f, 1f)
        val closest = a + ab * t
        return closest.distanceSquaredTo(point) <= radius * radius
    }

    override fun getVertices(position: Vec2, rotation: Float): List<Vec2> {
        val worldPos = position + offset
        val a = tipA.rotate(rotation) + worldPos
        val b = tipB.rotate(rotation) + worldPos
        val ab = (b - a).normalized()
        val perp = Vec2(-ab.y, ab.x)
        return listOf(
            a + perp * radius,
            b + perp * radius,
            b - perp * radius,
            a - perp * radius
        )
    }
}
