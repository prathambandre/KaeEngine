package com.kae.engine.physics

import com.kae.engine.scene.Component
import com.kae.engine.math.Vec2

data class GravityComponent(
    var gravity: Vec2 = Vec2(0f, -9.81f)
) : Component

data class PhysicsMaterial(
    var density: Float = 1f,
    var restitution: Float = 0.5f,
    var friction: Float = 0.3f
) : Component {
    companion object {
        val DEFAULT = PhysicsMaterial()
        val BOUNCY = PhysicsMaterial(restitution = 0.9f, friction = 0.1f)
        val SLIPPERY = PhysicsMaterial(restitution = 0.3f, friction = 0.05f)
        val RUBBER = PhysicsMaterial(restitution = 0.8f, friction = 0.9f)
        val ICE = PhysicsMaterial(restitution = 0.1f, friction = 0.02f)
    }
}

data class Transform2D(
    var position: Vec2 = Vec2(0f, 0f),
    var rotation: Float = 0f
) : Component

class SensorTag : Component

data class CollisionFilter(
    var layer: Int = 0x0001,
    var mask: Int = 0xFFFF
) : Component {
    fun shouldCollideWith(other: CollisionFilter): Boolean {
        return (layer and other.mask) != 0 && (other.layer and mask) != 0
    }
}
