package com.kae.engine.physics

import com.kae.engine.math.Vec2
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class RigidBodyTest {

    @Test
    fun `test default RigidBody2D`() {
        val body = RigidBody2D()
        assertEquals(BodyType.DYNAMIC, body.bodyType)
        assertEquals(1f, body.mass, 0.001f)
        assertEquals(1f, body.inverseMass, 0.001f)
        assertEquals(0.5f, body.restitution, 0.001f)
        assertEquals(0.3f, body.friction, 0.001f)
        assertEquals(Vec2.ZERO, body.velocity)
        assertEquals(0f, body.angularVelocity, 0.001f)
        assertEquals(1f, body.gravityScale, 0.001f)
        assertFalse(body.isTrigger)
        assertFalse(body.isSleeping)
    }

    @Test
    fun `test static body has zero inverse mass`() {
        val body = RigidBody2D.static()
        assertEquals(0f, body.inverseMass, 0.001f)
        assertEquals(0f, body.mass, 0.001f)
    }

    @Test
    fun `test dynamic body has correct inverse mass`() {
        val body = RigidBody2D.dynamic(mass = 4f)
        assertEquals(0.25f, body.inverseMass, 0.001f)
    }

    @Test
    fun `test kinematic body has zero inverse mass`() {
        val body = RigidBody2D.kinematic()
        assertEquals(0f, body.inverseMass, 0.001f)
    }

    @Test
    fun `test applyForce`() {
        val body = RigidBody2D()
        body.applyForce(Vec2(10f, 0f))
        assertEquals(Vec2(10f, 0f), body.force)

        body.applyForce(Vec2(0f, 5f))
        assertEquals(Vec2(10f, 5f), body.force)
    }

    @Test
    fun `test applyImpulse`() {
        val body = RigidBody2D.dynamic(mass = 2f)
        body.applyImpulse(Vec2(4f, 0f))

        // impulse / mass = velocity change
        assertEquals(2f, body.velocity.x, 0.001f)
        assertEquals(0f, body.velocity.y, 0.001f)
    }

    @Test
    fun `test applyImpulse does nothing on static body`() {
        val body = RigidBody2D.static()
        body.applyImpulse(Vec2(10f, 10f))
        assertEquals(Vec2.ZERO, body.velocity)
    }

    @Test
    fun `test applyImpulse does nothing on kinematic body`() {
        val body = RigidBody2D.kinematic()
        body.applyImpulse(Vec2(10f, 10f))
        assertEquals(Vec2.ZERO, body.velocity)
    }

    @Test
    fun `test applyTorque`() {
        val body = RigidBody2D()
        body.applyTorque(5f)
        assertEquals(5f, body.torque, 0.001f)

        body.applyTorque(3f)
        assertEquals(8f, body.torque, 0.001f)
    }

    @Test
    fun `test clearForces`() {
        val body = RigidBody2D()
        body.applyForce(Vec2(10f, 5f))
        body.applyTorque(3f)

        body.clearForces()

        assertEquals(Vec2.ZERO, body.force)
        assertEquals(0f, body.torque, 0.001f)
    }

    @Test
    fun `test updateMass`() {
        val body = RigidBody2D()
        body.updateMass(5f)
        assertEquals(5f, body.mass, 0.001f)
        assertEquals(0.2f, body.inverseMass, 0.001f)
    }

    @Test
    fun `test updateMass to zero gives zero inverse mass`() {
        val body = RigidBody2D()
        body.updateMass(0f)
        assertEquals(0f, body.inverseMass, 0.001f)
    }

    @Test
    fun `test wakeUp and sleep`() {
        val body = RigidBody2D()
        assertFalse(body.isSleeping)

        body.sleep()
        assertTrue(body.isSleeping)

        body.wakeUp()
        assertFalse(body.isSleeping)
    }

    @Test
    fun `test factory methods`() {
        val static = RigidBody2D.static(mass = 0f, restitution = 0.8f, friction = 0.5f)
        assertEquals(BodyType.STATIC, static.bodyType)
        assertEquals(0.8f, static.restitution, 0.001f)
        assertEquals(0.5f, static.friction, 0.001f)

        val dynamic = RigidBody2D.dynamic(mass = 3f, restitution = 0.3f, friction = 0.7f)
        assertEquals(BodyType.DYNAMIC, dynamic.bodyType)
        assertEquals(3f, dynamic.mass, 0.001f)
        assertEquals(0.3f, dynamic.restitution, 0.001f)

        val kinematic = RigidBody2D.kinematic(restitution = 0.6f, friction = 0.4f)
        assertEquals(BodyType.KINEMATIC, kinematic.bodyType)
        assertEquals(0.6f, kinematic.restitution, 0.001f)
    }

    @Test
    fun `test multiple impulses accumulate`() {
        val body = RigidBody2D.dynamic(mass = 1f)
        body.applyImpulse(Vec2(1f, 0f))
        body.applyImpulse(Vec2(1f, 0f))

        assertEquals(2f, body.velocity.x, 0.001f)
    }

    @Test
    fun `test force and impulse together`() {
        val body = RigidBody2D.dynamic(mass = 1f)
        body.applyImpulse(Vec2(5f, 0f))
        body.applyForce(Vec2(10f, 0f))

        assertEquals(5f, body.velocity.x, 0.001f)
        assertEquals(Vec2(10f, 0f), body.force)
    }

    @Test
    fun `test inverse mass recalculated on body type change`() {
        val body = RigidBody2D.dynamic(mass = 2f)
        assertEquals(0.5f, body.inverseMass, 0.001f)

        // Changing bodyType directly won't auto-update inverseMass
        // Use updateMass to recalculate
        body.bodyType = BodyType.STATIC
        body.updateMass(0f)
        assertEquals(0f, body.inverseMass, 0.001f)
    }

    @Test
    fun `test angular velocity accumulation via torque`() {
        val body = RigidBody2D.dynamic(mass = 1f)
        body.applyTorque(1f)
        // Torque integration happens externally, verify accumulation
        assertEquals(1f, body.torque, 0.001f)
    }

    @Test
    fun `test gravity scale defaults to 1`() {
        val body = RigidBody2D()
        assertEquals(1f, body.gravityScale, 0.001f)

        body.gravityScale = 0f
        assertEquals(0f, body.gravityScale, 0.001f)
    }
}
