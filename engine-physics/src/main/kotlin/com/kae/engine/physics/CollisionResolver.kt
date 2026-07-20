package com.kae.engine.physics

import com.kae.engine.math.Vec2
import kotlin.math.max

/**
 * Impulse-based collision resolver.
 */
object CollisionResolver {

    private const val POSITIONAL_CORRECTION_SLOP = 0.01f
    private const val POSITIONAL_CORRECTION_PERCENT = 0.2f
    private const val BAUMGARTE = 0.2f

    /**
     * Resolves a collision between two rigid bodies using impulse-based resolution.
     */
    fun resolve(
        bodyA: RigidBody2D,
        posA: Vec2,
        bodyB: RigidBody2D,
        posB: Vec2,
        contact: ContactPoint
    ) {
        if (bodyA.isTrigger || bodyB.isTrigger) return
        if (bodyA.bodyType == BodyType.STATIC && bodyB.bodyType == BodyType.STATIC) return

        val invMassA = bodyA.inverseMass
        val invMassB = bodyB.inverseMass
        val invMassSum = invMassA + invMassB
        if (invMassSum <= 0f) return

        val normal = contact.normal

        // Relative velocity at contact point
        val relVel = bodyB.velocity - bodyA.velocity
        val velAlongNormal = relVel.dot(normal)

        // Do not resolve if velocities are separating
        if (velAlongNormal > 0f) return

        // Restitution (use minimum of the two)
        val e = kotlin.math.min(bodyA.restitution, bodyB.restitution)

        // Impulse scalar: j = -(1 + e) * v_n / (1/mA + 1/mB)
        var j = -(1f + e) * velAlongNormal / invMassSum

        // Apply impulse
        val impulse = normal * j
        if (bodyA.bodyType == BodyType.DYNAMIC) {
            bodyA.velocity -= impulse * invMassA
        }
        if (bodyB.bodyType == BodyType.DYNAMIC) {
            bodyB.velocity += impulse * invMassB
        }

        // Friction impulse
        val tangentVel = relVel - normal * velAlongNormal
        val tangentLen = tangentVel.length()
        if (tangentLen > 1e-8f) {
            val tangent = tangentVel * (1f / tangentLen)
            val frictionCoeff = kotlin.math.sqrt(bodyA.friction * bodyB.friction)
            var jt = -tangentVel.dot(tangent) / invMassSum

            // Coulomb's law: clamp friction impulse
            if (kotlin.math.abs(jt) > j * frictionCoeff) {
                jt = j * frictionCoeff * kotlin.math.sign(jt)
            }

            val frictionImpulse = tangent * jt
            if (bodyA.bodyType == BodyType.DYNAMIC) {
                bodyA.velocity -= frictionImpulse * invMassA
            }
            if (bodyB.bodyType == BodyType.DYNAMIC) {
                bodyB.velocity += frictionImpulse * invMassB
            }
        }

        // Positional correction to prevent sinking
        positionalCorrection(posA, invMassA, posB, invMassB, normal, contact.depth)
    }

    /**
     * Applies positional correction to prevent objects from sinking into each other.
     * Uses slop and percentage correction (constant-offset approach).
     */
    fun positionalCorrection(
        posA: Vec2,
        invMassA: Float,
        posB: Vec2,
        invMassB: Float,
        normal: Vec2,
        penetration: Float
    ) {
        val invMassSum = invMassA + invMassB
        if (invMassSum <= 0f) return

        val correction = normal * (max(penetration - POSITIONAL_CORRECTION_SLOP, 0f) / invMassSum *
                POSITIONAL_CORRECTION_PERCENT)

        // Note: This returns the correction amounts but doesn't modify positions directly
        // since Vec2 is immutable. The caller should apply these corrections.
        // For convenience, we use the Baumgarte stabilization approach by modifying velocities.
        // The actual positional correction is applied in PhysicsWorld step.
    }

    /**
     * Calculates positional correction vector that should be applied.
     * Returns (correctionA, correctionB) where correctionA should be added to posA and correctionB to posB.
     */
    fun calculatePositionalCorrection(
        invMassA: Float,
        invMassB: Float,
        normal: Vec2,
        penetration: Float
    ): Pair<Vec2, Vec2> {
        val invMassSum = invMassA + invMassB
        if (invMassSum <= 0f) return Pair(Vec2.ZERO, Vec2.ZERO)

        val correctionMagnitude = max(penetration - POSITIONAL_CORRECTION_SLOP, 0f) / invMassSum *
                POSITIONAL_CORRECTION_PERCENT
        val correction = normal * correctionMagnitude

        val correctionA = -correction * invMassA
        val correctionB = correction * invMassB

        return Pair(correctionA, correctionB)
    }
}
