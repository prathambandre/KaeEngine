package com.kae.engine.physics

import com.kae.engine.scene.Component
import com.kae.engine.math.Vec2

enum class BodyType { STATIC, DYNAMIC, KINEMATIC }

data class RigidBody2D(
    var bodyType: BodyType = BodyType.DYNAMIC,
    var mass: Float = 1f,
    var restitution: Float = 0.5f,
    var friction: Float = 0.3f,
    var velocity: Vec2 = Vec2(0f, 0f),
    var angularVelocity: Float = 0f,
    var gravityScale: Float = 1f,
    var isTrigger: Boolean = false,
    var isSleeping: Boolean = false
) : Component {

    var inverseMass: Float = if (mass > 0f && bodyType == BodyType.DYNAMIC) 1f / mass else 0f
        private set

    var force: Vec2 = Vec2(0f, 0f)
    var torque: Float = 0f
    var sleepCounter: Int = 0

    fun applyForce(applyForce: Vec2) {
        force += applyForce
    }

    fun applyImpulse(impulse: Vec2) {
        if (bodyType != BodyType.DYNAMIC) return
        velocity += impulse * inverseMass
    }

    fun applyTorque(applyTorque: Float) {
        torque += applyTorque
    }

    fun clearForces() {
        force = Vec2(0f, 0f)
        torque = 0f
    }

    fun updateMass(newMass: Float) {
        mass = newMass
        inverseMass = if (mass > 0f && bodyType == BodyType.DYNAMIC) 1f / mass else 0f
    }

    fun wakeUp() {
        isSleeping = false
    }

    fun sleep() {
        isSleeping = true
    }

    companion object {
        fun static(mass: Float = 0f, restitution: Float = 0.5f, friction: Float = 0.3f): RigidBody2D {
            return RigidBody2D(
                bodyType = BodyType.STATIC,
                mass = 0f,
                restitution = restitution,
                friction = friction
            )
        }

        fun dynamic(mass: Float = 1f, restitution: Float = 0.5f, friction: Float = 0.3f): RigidBody2D {
            return RigidBody2D(
                bodyType = BodyType.DYNAMIC,
                mass = mass,
                restitution = restitution,
                friction = friction
            )
        }

        fun kinematic(restitution: Float = 0.5f, friction: Float = 0.3f): RigidBody2D {
            return RigidBody2D(
                bodyType = BodyType.KINEMATIC,
                mass = 0f,
                restitution = restitution,
                friction = friction
            )
        }
    }
}
