package com.kae.engine.physics

import com.kae.engine.math.Vec2
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CollisionResolverTest {

    @Test
    fun `test impulse resolution - head-on collision`() {
        val bodyA = RigidBody2D.dynamic(mass = 1f, restitution = 1f)
        bodyA.velocity = Vec2(1f, 0f)

        val bodyB = RigidBody2D.dynamic(mass = 1f, restitution = 1f)
        bodyB.velocity = Vec2(-1f, 0f)

        val contact = ContactPoint(
            point = Vec2(0.5f, 0f),
            normal = Vec2(1f, 0f),
            depth = 0.1f
        )

        CollisionResolver.resolve(bodyA, Vec2(0f, 0f), bodyB, Vec2(1f, 0f), contact)

        // After elastic collision between equal masses, velocities should swap
        assertTrue(bodyA.velocity.x < 0f)
        assertTrue(bodyB.velocity.x > 0f)
    }

    @Test
    fun `test impulse resolution - static body`() {
        val bodyA = RigidBody2D.static()
        val bodyB = RigidBody2D.dynamic(mass = 1f, restitution = 0.5f)
        bodyB.velocity = Vec2(-2f, 0f)

        val contact = ContactPoint(
            point = Vec2(0f, 0f),
            normal = Vec2(1f, 0f),
            depth = 0.05f
        )

        CollisionResolver.resolve(bodyA, Vec2(-1f, 0f), bodyB, Vec2(1f, 0f), contact)

        // Static body should not move
        assertEquals(0f, bodyA.velocity.x, 0.001f)
        assertEquals(0f, bodyA.velocity.y, 0.001f)
        // Dynamic body should bounce
        assertTrue(bodyB.velocity.x > 0f)
    }

    @Test
    fun `test impulse resolution - both static`() {
        val bodyA = RigidBody2D.static()
        val bodyB = RigidBody2D.static()

        val contact = ContactPoint(Vec2.ZERO, Vec2(1f, 0f), 0.1f)

        CollisionResolver.resolve(bodyA, Vec2.ZERO, bodyB, Vec2(1f, 0f), contact)

        assertEquals(0f, bodyA.velocity.x, 0.001f)
        assertEquals(0f, bodyB.velocity.x, 0.001f)
    }

    @Test
    fun `test impulse resolution - separation (no collision)`() {
        val bodyA = RigidBody2D.dynamic(mass = 1f)
        bodyA.velocity = Vec2(-1f, 0f)

        val bodyB = RigidBody2D.dynamic(mass = 1f)
        bodyB.velocity = Vec2(1f, 0f)

        val contact = ContactPoint(Vec2.ZERO, Vec2(1f, 0f), 0.1f)

        val velAx = bodyA.velocity.x
        val velBx = bodyB.velocity.x
        CollisionResolver.resolve(bodyA, Vec2.ZERO, bodyB, Vec2(1f, 0f), contact)
        assertEquals(velAx, bodyA.velocity.x, 0.001f)
        assertEquals(velBx, bodyB.velocity.x, 0.001f)
    }

    @Test
    fun `test impulse resolution - trigger bodies do not resolve`() {
        val bodyA = RigidBody2D.dynamic(mass = 1f)
        bodyA.velocity = Vec2(1f, 0f)
        bodyA.isTrigger = true

        val bodyB = RigidBody2D.dynamic(mass = 1f)
        bodyB.velocity = Vec2(-1f, 0f)

        val contact = ContactPoint(Vec2.ZERO, Vec2(1f, 0f), 0.1f)

        CollisionResolver.resolve(bodyA, Vec2.ZERO, bodyB, Vec2(1f, 0f), contact)

        // Trigger should not cause velocity change
        assertEquals(1f, bodyA.velocity.x, 0.001f)
        assertEquals(-1f, bodyB.velocity.x, 0.001f)
    }

    @Test
    fun `test impulse resolution - restitution affects bounce`() {
        val bodyA = RigidBody2D.dynamic(mass = 1f, restitution = 0f)
        val bodyB = RigidBody2D.static()

        bodyA.velocity = Vec2(1f, 0f)

        val contact = ContactPoint(Vec2.ZERO, Vec2(1f, 0f), 0.1f)

        CollisionResolver.resolve(bodyA, Vec2(-1f, 0f), bodyB, Vec2(1f, 0f), contact)

        // With 0 restitution, dynamic body should stop against static
        assertEquals(0f, bodyA.velocity.x, 0.01f)
    }

    @Test
    fun `test impulse resolution - high restitution bounces`() {
        val bodyA = RigidBody2D.dynamic(mass = 1f, restitution = 1f)
        val bodyB = RigidBody2D.static(restitution = 1f)

        bodyA.velocity = Vec2(1f, 0f)

        val contact = ContactPoint(Vec2.ZERO, Vec2(1f, 0f), 0.1f)

        CollisionResolver.resolve(bodyA, Vec2(-1f, 0f), bodyB, Vec2(1f, 0f), contact)

        // With 1.0 restitution, dynamic body should bounce at full speed
        assertEquals(-1f, bodyA.velocity.x, 0.01f)
    }

    @Test
    fun `test positional correction - correction direction`() {
        val (corrA, corrB) = CollisionResolver.calculatePositionalCorrection(
            invMassA = 1f,
            invMassB = 0f,
            normal = Vec2(1f, 0f),
            penetration = 0.1f
        )

        // Correction should push body A away from B (opposite to normal)
        assertTrue(corrA.x < 0f)
        // Static body should not be corrected
        assertEquals(0f, corrB.x, 0.001f)
        assertEquals(0f, corrB.y, 0.001f)
    }

    @Test
    fun `test positional correction - both dynamic`() {
        val (corrA, corrB) = CollisionResolver.calculatePositionalCorrection(
            invMassA = 1f,
            invMassB = 1f,
            normal = Vec2(0f, 1f),
            penetration = 0.2f
        )

        // Both should be corrected, in opposite directions
        assertTrue(corrA.y < 0f)
        assertTrue(corrB.y > 0f)
    }

    @Test
    fun `test positional correction - zero penetration`() {
        val (corrA, corrB) = CollisionResolver.calculatePositionalCorrection(
            invMassA = 1f,
            invMassB = 1f,
            normal = Vec2(1f, 0f),
            penetration = 0f
        )

        // No correction needed
        assertEquals(Vec2.ZERO, corrA)
        assertEquals(Vec2.ZERO, corrB)
    }

    @Test
    fun `test positional correction - inverse mass sum zero`() {
        val (corrA, corrB) = CollisionResolver.calculatePositionalCorrection(
            invMassA = 0f,
            invMassB = 0f,
            normal = Vec2(1f, 0f),
            penetration = 0.5f
        )

        assertEquals(Vec2.ZERO, corrA)
        assertEquals(Vec2.ZERO, corrB)
    }

    @Test
    fun `test impulse resolution - friction reduces tangential velocity`() {
        val bodyA = RigidBody2D.dynamic(mass = 1f, restitution = 1f, friction = 1f)
        val bodyB = RigidBody2D.static()

        bodyA.velocity = Vec2(1f, -1f)

        val contact = ContactPoint(Vec2.ZERO, Vec2(1f, 0f), 0.1f)

        CollisionResolver.resolve(bodyA, Vec2(-1f, 0f), bodyB, Vec2(1f, 0f), contact)

        // Normal should reverse x (push away), friction should reduce y
        assertTrue(bodyA.velocity.x < 0f)
        assertTrue(kotlin.math.abs(bodyA.velocity.y) < 1f)
    }

    @Test
    fun `test impulse resolution - equal mass head-on swap`() {
        val bodyA = RigidBody2D.dynamic(mass = 2f, restitution = 1f)
        bodyA.velocity = Vec2(5f, 0f)

        val bodyB = RigidBody2D.dynamic(mass = 2f, restitution = 1f)
        bodyB.velocity = Vec2(-3f, 0f)

        val contact = ContactPoint(Vec2(0.5f, 0f), Vec2(1f, 0f), 0.1f)

        CollisionResolver.resolve(bodyA, Vec2(0f, 0f), bodyB, Vec2(1f, 0f), contact)

        // Equal mass elastic collision: velocities should swap
        assertEquals(-3f, bodyA.velocity.x, 0.1f)
        assertEquals(5f, bodyB.velocity.x, 0.1f)
    }

    @Test
    fun `test impulse resolution - kinematic body behavior`() {
        val bodyA = RigidBody2D.kinematic()
        val bodyB = RigidBody2D.dynamic(mass = 1f, restitution = 0.5f)
        bodyB.velocity = Vec2(-2f, 0f)

        val contact = ContactPoint(Vec2.ZERO, Vec2(1f, 0f), 0.05f)

        CollisionResolver.resolve(bodyA, Vec2(-1f, 0f), bodyB, Vec2(1f, 0f), contact)

        // Kinematic body inverse mass is 0, so only dynamic body should be affected
        assertEquals(0f, bodyA.velocity.x, 0.001f)
        assertTrue(bodyB.velocity.x > 0f)
    }
}
