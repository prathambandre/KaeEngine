package com.kae.engine.physics

import com.kae.engine.scene.Entity
import com.kae.engine.math.Vec2

data class RayCastResult(
    val entity: Entity,
    val point: Vec2,
    val normal: Vec2,
    val distance: Float
)

data class CollisionPair(
    val entityA: Entity,
    val entityB: Entity
)

class PhysicsWorld(
    var gravity: Vec2 = Vec2(0f, -9.81f)
) {

    private data class BodyEntry(
        val entity: Entity,
        val body: RigidBody2D,
        var position: Vec2,
        var rotation: Float,
        val collider: Collider2D
    )

    private val bodies = mutableListOf<BodyEntry>()
    private var quadtree = Quadtree(
        AABB(Vec2(-10000f, -10000f), Vec2(10000f, 10000f))
    )
    private val previousCollisionPairs = mutableSetOf<CollisionPair>()
    private val currentCollisionPairs = mutableSetOf<CollisionPair>()

    var onCollisionEnter: ((Entity, Entity, List<ContactPoint>) -> Unit)? = null
    var onCollisionStay: ((Entity, Entity, List<ContactPoint>) -> Unit)? = null
    var onCollisionExit: ((Entity, Entity) -> Unit)? = null

    fun addBody(entity: Entity, body: RigidBody2D, collider: Collider2D,
                position: Vec2 = Vec2.ZERO, rotation: Float = 0f) {
        bodies.add(BodyEntry(entity, body, position, rotation, collider))
    }

    fun removeBody(entity: Entity) {
        bodies.removeAll { it.entity == entity }
    }

    fun updateBodyTransform(entity: Entity, position: Vec2, rotation: Float) {
        bodies.find { it.entity == entity }?.let {
            it.position = position
            it.rotation = rotation
        }
    }

    fun getBodyPosition(entity: Entity): Vec2? {
        return bodies.find { it.entity == entity }?.position
    }

    fun getBodyRotation(entity: Entity): Float? {
        return bodies.find { it.entity == entity }?.rotation
    }

    fun step(dt: Float) {
        if (bodies.isEmpty()) return

        for (entry in bodies) {
            if (entry.body.isSleeping) continue
            if (entry.body.bodyType == BodyType.DYNAMIC) {
                integrate(entry.body, entry.position, dt)
            }
        }

        quadtree.clear()
        for (entry in bodies) {
            val bounds = entry.collider.getBounds(entry.position, entry.rotation)
            quadtree.insert(entry.entity, bounds)
        }

        currentCollisionPairs.clear()
        val testedPairs = mutableSetOf<CollisionPair>()

        for (entry in bodies) {
            if (entry.body.isSleeping) continue
            val bounds = entry.collider.getBounds(entry.position, entry.rotation)
            val candidates = quadtree.query(bounds)

            for (candidate in candidates) {
                if (candidate.entity == entry.entity) continue

                val pairKey = if (entry.entity.id < candidate.entity.id) {
                    CollisionPair(entry.entity, candidate.entity)
                } else {
                    CollisionPair(candidate.entity, entry.entity)
                }

                if (!testedPairs.add(pairKey)) continue

                val entryB = bodies.find { it.entity == candidate.entity } ?: continue

                val result = CollisionDetector.testCollision(
                    entry.collider, entry.position, entry.rotation,
                    entryB.collider, entryB.position, entryB.rotation
                )

                if (result.colliding) {
                    currentCollisionPairs.add(pairKey)

                    if (!entry.body.isTrigger && !entryB.body.isTrigger) {
                        for (contact in result.contactPoints) {
                            CollisionResolver.resolve(
                                entry.body, entry.position,
                                entryB.body, entryB.position,
                                contact
                            )
                        }

                        for (contact in result.contactPoints) {
                            val (corrA, corrB) = CollisionResolver.calculatePositionalCorrection(
                                entry.body.inverseMass, entryB.body.inverseMass,
                                contact.normal, contact.depth
                            )
                            if (entry.body.bodyType == BodyType.DYNAMIC) {
                                entry.position += corrA
                            }
                            if (entryB.body.bodyType == BodyType.DYNAMIC) {
                                entryB.position += corrB
                            }
                        }
                    }

                    if (pairKey in previousCollisionPairs) {
                        onCollisionStay?.invoke(pairKey.entityA, pairKey.entityB, result.contactPoints)
                    } else {
                        onCollisionEnter?.invoke(pairKey.entityA, pairKey.entityB, result.contactPoints)
                    }
                }
            }
        }

        for (pair in previousCollisionPairs) {
            if (pair !in currentCollisionPairs) {
                onCollisionExit?.invoke(pair.entityA, pair.entityB)
            }
        }
        previousCollisionPairs.clear()
        previousCollisionPairs.addAll(currentCollisionPairs)

        for (entry in bodies) {
            if (entry.body.isSleeping) continue
            if (entry.body.bodyType == BodyType.DYNAMIC) {
                entry.position += entry.body.velocity * dt
                entry.rotation += entry.body.angularVelocity * dt
            } else if (entry.body.bodyType == BodyType.KINEMATIC) {
                entry.position += entry.body.velocity * dt
                entry.rotation += entry.body.angularVelocity * dt
            }
        }
    }

    fun integrate(body: RigidBody2D, position: Vec2, dt: Float) {
        if (body.bodyType != BodyType.DYNAMIC) return
        if (body.isSleeping) return

        val gravityForce = gravity * body.gravityScale
        body.velocity += gravityForce * dt
        body.velocity += body.force * body.inverseMass * dt
        body.angularVelocity += body.torque * body.inverseMass * dt
        body.velocity *= 0.999f
        body.angularVelocity *= 0.999f
        body.clearForces()

        if (body.velocity.lengthSquared() < 0.001f && kotlin.math.abs(body.angularVelocity) < 0.001f) {
            body.sleepCounter++
            if (body.sleepCounter > 60) {
                body.velocity = Vec2.ZERO
                body.angularVelocity = 0f
                body.sleep()
            }
        } else {
            body.sleepCounter = 0
        }
    }

    fun rayCast(origin: Vec2, direction: Vec2, maxDistance: Float = Float.MAX_VALUE): RayCastResult? {
        val dirNorm = direction.normalized()
        val endPoint = origin + dirNorm * maxDistance
        val rayBounds = AABB(
            Vec2(kotlin.math.min(origin.x, endPoint.x), kotlin.math.min(origin.y, endPoint.y)),
            Vec2(kotlin.math.max(origin.x, endPoint.x), kotlin.math.max(origin.y, endPoint.y))
        )

        val candidates = quadtree.query(rayBounds)
        var closestResult: RayCastResult? = null
        var closestDist = maxDistance

        for (candidate in candidates) {
            val hit = rayCastVsAABB(origin, dirNorm, candidate.bounds)
            if (hit != null && hit.distance < closestDist) {
                closestDist = hit.distance
                closestResult = RayCastResult(
                    entity = candidate.entity,
                    point = hit.point,
                    normal = hit.normal,
                    distance = hit.distance
                )
            }
        }

        return closestResult
    }

    private data class RayHit(val point: Vec2, val normal: Vec2, val distance: Float)

    private fun rayCastVsAABB(origin: Vec2, direction: Vec2, bounds: AABB): RayHit? {
        var tmin = 0f
        var tmax = Float.MAX_VALUE

        val invDirX = if (kotlin.math.abs(direction.x) > 1e-10f) 1f / direction.x else Float.MAX_VALUE
        val invDirY = if (kotlin.math.abs(direction.y) > 1e-10f) 1f / direction.y else Float.MAX_VALUE

        var nearNormal = Vec2.ZERO

        val t1 = (bounds.min.x - origin.x) * invDirX
        val t2 = (bounds.max.x - origin.x) * invDirX
        if (t1 < t2) {
            if (t1 > tmin) { tmin = t1; nearNormal = Vec2(-1f, 0f) }
            if (t2 < tmax) tmax = t2
        } else {
            if (t2 > tmin) { tmin = t2; nearNormal = Vec2(1f, 0f) }
            if (t1 < tmax) tmax = t1
        }

        val t3 = (bounds.min.y - origin.y) * invDirY
        val t4 = (bounds.max.y - origin.y) * invDirY
        if (t3 < t4) {
            if (t3 > tmin) { tmin = t3; nearNormal = Vec2(0f, -1f) }
            if (t4 < tmax) tmax = t4
        } else {
            if (t4 > tmin) { tmin = t4; nearNormal = Vec2(0f, 1f) }
            if (t3 < tmax) tmax = t3
        }

        if (tmin > tmax || tmax < 0f) return null

        val t = if (tmin >= 0f) tmin else tmax
        val point = origin + direction * t
        return RayHit(point, nearNormal, t)
    }

    fun clearBodies() {
        bodies.clear()
        quadtree.clear()
        previousCollisionPairs.clear()
        currentCollisionPairs.clear()
    }

    fun bodyCount(): Int = bodies.size

    fun getEntities(): List<Entity> = bodies.map { it.entity }
}
