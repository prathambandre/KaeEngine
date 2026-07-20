package com.kae.engine.physics

import com.kae.engine.math.Vec2
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Represents a single contact point between two colliding bodies.
 */
data class ContactPoint(
    val point: Vec2,
    val normal: Vec2,
    val depth: Float
)

/**
 * Result of a collision test.
 */
data class CollisionResult(
    val colliding: Boolean,
    val contactPoints: List<ContactPoint> = emptyList()
)

/**
 * Narrow-phase collision detection functions.
 */
object CollisionDetector {

    /**
     * Tests two AABBs for overlap.
     */
    fun testAABBvsAABB(a: AABB, b: AABB): CollisionResult {
        if (!a.intersects(b)) return CollisionResult(false)

        val overlap = a.overlap(b) ?: return CollisionResult(false)

        val centerA = a.center
        val centerB = b.center
        var normal = centerB - centerA
        val len = normal.length()
        if (len < 1e-8f) {
            normal = Vec2(1f, 0f)
        } else {
            normal = normal * (1f / len)
        }

        val contactPoint = overlap.center
        val depth = min(overlap.size.x, overlap.size.y)

        return CollisionResult(
            colliding = true,
            contactPoints = listOf(ContactPoint(contactPoint, normal, depth))
        )
    }

    /**
     * Tests two circles for overlap.
     */
    fun testCirclevsCircle(posA: Vec2, radiusA: Float, posB: Vec2, radiusB: Float): CollisionResult {
        val diff = posB - posA
        val distSq = diff.lengthSquared()
        val radiusSum = radiusA + radiusB

        if (distSq > radiusSum * radiusSum) return CollisionResult(false)

        val dist = sqrt(distSq)
        val normal: Vec2
        val depth: Float
        val contactPoint: Vec2

        if (dist < 1e-8f) {
            normal = Vec2(1f, 0f)
            depth = radiusSum
            contactPoint = posA
        } else {
            normal = diff * (1f / dist)
            depth = radiusSum - dist
            contactPoint = posA + normal * radiusA
        }

        return CollisionResult(
            colliding = true,
            contactPoints = listOf(ContactPoint(contactPoint, normal, depth))
        )
    }

    /**
     * Tests an AABB against a circle.
     */
    fun testAABBvsCircle(aabbPos: Vec2, halfExtents: Vec2, circlePos: Vec2, radius: Float): CollisionResult {
        val diff = circlePos - aabbPos
        val clampedX = diff.x.coerceIn(-halfExtents.x, halfExtents.x)
        val clampedY = diff.y.coerceIn(-halfExtents.y, halfExtents.y)
        val closestPoint = aabbPos + Vec2(clampedX, clampedY)
        val diffClosest = circlePos - closestPoint
        val distSq = diffClosest.lengthSquared()

        if (distSq > radius * radius) return CollisionResult(false)

        val dist = sqrt(distSq)
        val normal: Vec2
        val depth: Float

        if (dist < 1e-8f) {
            val dx = halfExtents.x - abs(diff.x)
            val dy = halfExtents.y - abs(diff.y)
            if (dx < dy) {
                normal = Vec2(if (diff.x > 0f) 1f else -1f, 0f)
                depth = dx + radius
            } else {
                normal = Vec2(0f, if (diff.y > 0f) 1f else -1f)
                depth = dy + radius
            }
        } else {
            normal = diffClosest * (1f / dist)
            depth = radius - dist
        }

        return CollisionResult(
            colliding = true,
            contactPoints = listOf(ContactPoint(closestPoint, normal, depth))
        )
    }

    /**
     * Tests two arbitrary colliders for collision using appropriate algorithms.
     */
    fun testCollision(
        colliderA: Collider2D,
        posA: Vec2,
        rotA: Float,
        colliderB: Collider2D,
        posB: Vec2,
        rotB: Float
    ): CollisionResult {
        return when {
            colliderA is CircleCollider2D && colliderB is CircleCollider2D -> {
                val worldA = posA + colliderA.offset
                val worldB = posB + colliderB.offset
                testCirclevsCircle(worldA, colliderA.radius, worldB, colliderB.radius)
            }
            colliderA is BoxCollider2D && colliderB is BoxCollider2D -> {
                testRotatedBoxVsRotatedBox(colliderA, posA, rotA, colliderB, posB, rotB)
            }
            colliderA is BoxCollider2D && colliderB is CircleCollider2D -> {
                val worldBox = posA + colliderA.offset
                val worldCircle = posB + colliderB.offset
                testAABBvsCircle(worldBox, colliderA.halfExtents, worldCircle, colliderB.radius)
            }
            colliderA is CircleCollider2D && colliderB is BoxCollider2D -> {
                val worldCircle = posA + colliderA.offset
                val worldBox = posB + colliderB.offset
                val result = testAABBvsCircle(worldBox, colliderB.halfExtents, worldCircle, colliderA.radius)
                if (result.colliding) {
                    CollisionResult(true, result.contactPoints.map { ContactPoint(it.point, -it.normal, it.depth) })
                } else {
                    result
                }
            }
            colliderA is CapsuleCollider2D && colliderB is CircleCollider2D -> {
                testCapsuleVsCircle(colliderA, posA, rotA, colliderB, posB, rotB)
            }
            colliderA is CircleCollider2D && colliderB is CapsuleCollider2D -> {
                val result = testCapsuleVsCircle(colliderB, posB, rotB, colliderA, posA, rotA)
                if (result.colliding) {
                    CollisionResult(true, result.contactPoints.map { ContactPoint(it.point, -it.normal, it.depth) })
                } else {
                    result
                }
            }
            colliderA is CapsuleCollider2D && colliderB is CapsuleCollider2D -> {
                testCapsuleVsCapsule(colliderA, posA, rotA, colliderB, posB, rotB)
            }
            colliderA is CapsuleCollider2D && colliderB is BoxCollider2D -> {
                testCapsuleVsBox(colliderA, posA, rotA, colliderB, posB, rotB)
            }
            colliderA is BoxCollider2D && colliderB is CapsuleCollider2D -> {
                val result = testCapsuleVsBox(colliderB, posB, rotB, colliderA, posA, rotA)
                if (result.colliding) {
                    CollisionResult(true, result.contactPoints.map { ContactPoint(it.point, -it.normal, it.depth) })
                } else {
                    result
                }
            }
            else -> CollisionResult(false)
        }
    }

    /**
     * SAT-based collision test for two rotated boxes.
     */
    private fun testRotatedBoxVsRotatedBox(
        boxA: BoxCollider2D,
        posA: Vec2,
        rotA: Float,
        boxB: BoxCollider2D,
        posB: Vec2,
        rotB: Float
    ): CollisionResult {
        val vertsA = boxA.getVertices(posA, rotA)
        val vertsB = boxB.getVertices(posB, rotB)

        val axes = mutableListOf<Vec2>()
        axes.addAll(getAxesFromVertices(vertsA))
        axes.addAll(getAxesFromVertices(vertsB))

        var minOverlap = Float.MAX_VALUE
        var minAxis = Vec2.ZERO

        for (axis in axes) {
            val projA = projectVertices(vertsA, axis)
            val projB = projectVertices(vertsB, axis)
            val overlap = min(projA.second, projB.second) - max(projA.first, projB.first)

            if (overlap <= 0f) return CollisionResult(false)

            if (overlap < minOverlap) {
                minOverlap = overlap
                minAxis = axis
            }
        }

        var normal = minAxis
        val d = posB - posA
        if (d.dot(normal) < 0f) {
            normal = -normal
        }

        val contactPoint = computeContactPoint(vertsA, vertsB)

        return CollisionResult(
            colliding = true,
            contactPoints = listOf(ContactPoint(contactPoint, normal, minOverlap))
        )
    }

    /**
     * Tests a capsule against a circle.
     */
    private fun testCapsuleVsCircle(
        capsule: CapsuleCollider2D,
        capsulePos: Vec2,
        capsuleRot: Float,
        circle: CircleCollider2D,
        circlePos: Vec2,
        circleRot: Float
    ): CollisionResult {
        val worldPos = capsulePos + capsule.offset
        val a = capsule.tipA.rotate(capsuleRot) + worldPos
        val b = capsule.tipB.rotate(capsuleRot) + worldPos
        val circleWorld = circlePos + circle.offset

        val closestOnSegment = closestPointOnSegment(a, b, circleWorld)
        val diff = circleWorld - closestOnSegment
        val distSq = diff.lengthSquared()
        val radiusSum = capsule.radius + circle.radius

        if (distSq > radiusSum * radiusSum) return CollisionResult(false)

        val dist = sqrt(distSq)
        val normal: Vec2
        val depth: Float

        if (dist < 1e-8f) {
            val ab = (b - a).normalized()
            normal = Vec2(-ab.y, ab.x)
            depth = radiusSum
        } else {
            normal = diff * (1f / dist)
            depth = radiusSum - dist
        }

        val contactPoint = closestOnSegment + normal * capsule.radius
        return CollisionResult(
            colliding = true,
            contactPoints = listOf(ContactPoint(contactPoint, normal, depth))
        )
    }

    /**
     * Tests two capsules against each other (segment-segment closest points approach).
     */
    private fun testCapsuleVsCapsule(
        capA: CapsuleCollider2D,
        posA: Vec2,
        rotA: Float,
        capB: CapsuleCollider2D,
        posB: Vec2,
        rotB: Float
    ): CollisionResult {
        val worldA = posA + capA.offset
        val a1 = capA.tipA.rotate(rotA) + worldA
        val a2 = capA.tipB.rotate(rotA) + worldA

        val worldB = posB + capB.offset
        val b1 = capB.tipA.rotate(rotB) + worldB
        val b2 = capB.tipB.rotate(rotB) + worldB

        val closestA = closestPointOnSegment(a1, a2, closestPointOnSegment(b1, b2, a1))
        val closestB = closestPointOnSegment(b1, b2, closestA)

        val diff = closestB - closestA
        val distSq = diff.lengthSquared()
        val radiusSum = capA.radius + capB.radius

        if (distSq > radiusSum * radiusSum) return CollisionResult(false)

        val dist = sqrt(distSq)
        val normal: Vec2
        val depth: Float

        if (dist < 1e-8f) {
            val ab = (a2 - a1).normalized()
            normal = Vec2(-ab.y, ab.x)
            depth = radiusSum
        } else {
            normal = diff * (1f / dist)
            depth = radiusSum - dist
        }

        val contactPoint = closestA + normal * capA.radius
        return CollisionResult(
            colliding = true,
            contactPoints = listOf(ContactPoint(contactPoint, normal, depth))
        )
    }

    /**
     * Tests a capsule against a box.
     */
    private fun testCapsuleVsBox(
        capsule: CapsuleCollider2D,
        capsulePos: Vec2,
        capsuleRot: Float,
        box: BoxCollider2D,
        boxPos: Vec2,
        boxRot: Float
    ): CollisionResult {
        val worldCapsulePos = capsulePos + capsule.offset
        val a = capsule.tipA.rotate(capsuleRot) + worldCapsulePos
        val b = capsule.tipB.rotate(capsuleRot) + worldCapsulePos

        val boxWorldPos = boxPos + box.offset
        val boxVerts = box.getVertices(boxPos, boxRot)

        var minDepth = Float.MAX_VALUE
        var bestNormal = Vec2.ZERO
        var bestContact = Vec2.ZERO

        for (i in boxVerts.indices) {
            val v1 = boxVerts[i]
            val v2 = boxVerts[(i + 1) % boxVerts.size]
            val closest = closestPointOnSegment(v1, v2, a)
            val diff = closest - a
            val distSq = diff.lengthSquared()
            if (distSq < minDepth) {
                minDepth = distSq
                val dist = sqrt(distSq)
                bestNormal = if (dist > 1e-8f) diff * (1f / dist) else Vec2(1f, 0f)
                bestContact = closest
            }
        }

        for (i in boxVerts.indices) {
            val v1 = boxVerts[i]
            val v2 = boxVerts[(i + 1) % boxVerts.size]
            val closest = closestPointOnSegment(v1, v2, b)
            val diff = closest - b
            val distSq = diff.lengthSquared()
            if (distSq < minDepth) {
                minDepth = distSq
                val dist = sqrt(distSq)
                bestNormal = if (dist > 1e-8f) diff * (1f / dist) else Vec2(1f, 0f)
                bestContact = closest
            }
        }

        val depth = sqrt(minDepth) - capsule.radius
        if (depth > 0f) return CollisionResult(false)

        val center = boxWorldPos
        val d = a - center
        if (d.dot(bestNormal) > 0f) {
            bestNormal = -bestNormal
        }

        return CollisionResult(
            colliding = true,
            contactPoints = listOf(ContactPoint(bestContact, bestNormal, -depth))
        )
    }

    // --- Helper functions ---

    private fun getAxesFromVertices(verts: List<Vec2>): List<Vec2> {
        val axes = mutableListOf<Vec2>()
        for (i in verts.indices) {
            val edge = verts[(i + 1) % verts.size] - verts[i]
            axes.add(Vec2(-edge.y, edge.x).normalized())
        }
        return axes
    }

    private fun projectVertices(verts: List<Vec2>, axis: Vec2): Pair<Float, Float> {
        var minProj = Float.MAX_VALUE
        var maxProj = Float.MIN_VALUE
        for (v in verts) {
            val proj = v.dot(axis)
            if (proj < minProj) minProj = proj
            if (proj > maxProj) maxProj = proj
        }
        return Pair(minProj, maxProj)
    }

    private fun computeContactPoint(vertsA: List<Vec2>, vertsB: List<Vec2>): Vec2 {
        var closestPoint = vertsA[0]
        var minDistSq = Float.MAX_VALUE
        for (vA in vertsA) {
            for (vB in vertsB) {
                val dSq = vA.distanceSquaredTo(vB)
                if (dSq < minDistSq) {
                    minDistSq = dSq
                    closestPoint = (vA + vB) * 0.5f
                }
            }
        }
        return closestPoint
    }

    private fun closestPointOnSegment(a: Vec2, b: Vec2, point: Vec2): Vec2 {
        val ab = b - a
        val lenSq = ab.lengthSquared()
        if (lenSq < 1e-10f) return a
        val t = ((point - a).dot(ab) / lenSq).coerceIn(0f, 1f)
        return a + ab * t
    }
}
