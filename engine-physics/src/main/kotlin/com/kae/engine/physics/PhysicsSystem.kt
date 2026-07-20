package com.kae.engine.physics

import com.kae.engine.scene.System
import com.kae.engine.scene.World
import com.kae.engine.scene.Entity
import com.kae.engine.math.Vec2

class PhysicsSystem(
    var gravity: Vec2 = Vec2(0f, -9.81f)
) : System() {

    override val priority: Int = -10

    val physicsWorld: PhysicsWorld = PhysicsWorld(gravity)
    var fixedDeltaTime: Float = 1f / 60f
    var enableDebugDraw: Boolean = false

    private var accumulator: Float = 0f

    override fun init(world: World) {
        physicsWorld.gravity = gravity
    }

    override fun update(world: World, deltaTime: Float) {
        physicsWorld.gravity = gravity
        accumulator += deltaTime

        var steps = 0
        while (accumulator >= fixedDeltaTime && steps < MAX_STEPS) {
            physicsWorld.step(fixedDeltaTime)
            accumulator -= fixedDeltaTime
            steps++
        }
    }

    fun addBody(
        entity: Entity,
        body: RigidBody2D,
        collider: Collider2D,
        position: Vec2 = Vec2.ZERO,
        rotation: Float = 0f
    ) {
        physicsWorld.addBody(entity, body, collider, position, rotation)
    }

    fun removeBody(entity: Entity) {
        physicsWorld.removeBody(entity)
    }

    fun updateBodyTransform(entity: Entity, position: Vec2, rotation: Float) {
        physicsWorld.updateBodyTransform(entity, position, rotation)
    }

    fun integrate(body: RigidBody2D, position: Vec2, dt: Float) {
        physicsWorld.integrate(body, position, dt)
    }

    fun rayCast(origin: Vec2, direction: Vec2, maxDistance: Float = Float.MAX_VALUE): RayCastResult? {
        return physicsWorld.rayCast(origin, direction, maxDistance)
    }

    override fun destroy() {
        physicsWorld.clearBodies()
        accumulator = 0f
    }

    companion object {
        private const val MAX_STEPS = 5
    }
}
