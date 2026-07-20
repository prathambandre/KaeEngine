package com.kae.engine.physics

import com.kae.engine.math.Vec2
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CollisionDetectorTest {

    @Test
    fun `test AABB vs AABB - overlapping`() {
        val a = AABB(Vec2(0f, 0f), Vec2(2f, 2f))
        val b = AABB(Vec2(1f, 1f), Vec2(3f, 3f))
        val result = CollisionDetector.testAABBvsAABB(a, b)
        assertTrue(result.colliding)
        assertEquals(1, result.contactPoints.size)
        assertTrue(result.contactPoints[0].depth > 0f)
    }

    @Test
    fun `test AABB vs AABB - not overlapping`() {
        val a = AABB(Vec2(0f, 0f), Vec2(1f, 1f))
        val b = AABB(Vec2(3f, 3f), Vec2(4f, 4f))
        val result = CollisionDetector.testAABBvsAABB(a, b)
        assertFalse(result.colliding)
        assertTrue(result.contactPoints.isEmpty())
    }

    @Test
    fun `test AABB vs AABB - touching`() {
        val a = AABB(Vec2(0f, 0f), Vec2(1f, 1f))
        val b = AABB(Vec2(1f, 0f), Vec2(2f, 1f))
        val result = CollisionDetector.testAABBvsAABB(a, b)
        assertTrue(result.colliding)
    }

    @Test
    fun `test AABB vs AABB - one inside other`() {
        val a = AABB(Vec2(0f, 0f), Vec2(10f, 10f))
        val b = AABB(Vec2(3f, 3f), Vec2(5f, 5f))
        val result = CollisionDetector.testAABBvsAABB(a, b)
        assertTrue(result.colliding)
        assertTrue(result.contactPoints[0].depth > 0f)
    }

    @Test
    fun `test Circle vs Circle - overlapping`() {
        val result = CollisionDetector.testCirclevsCircle(
            Vec2(0f, 0f), 1f,
            Vec2(1f, 0f), 1f
        )
        assertTrue(result.colliding)
        assertEquals(1, result.contactPoints.size)
        assertEquals(1f, result.contactPoints[0].depth, 0.001f)
    }

    @Test
    fun `test Circle vs Circle - not overlapping`() {
        val result = CollisionDetector.testCirclevsCircle(
            Vec2(0f, 0f), 1f,
            Vec2(5f, 0f), 1f
        )
        assertFalse(result.colliding)
    }

    @Test
    fun `test Circle vs Circle - touching`() {
        val result = CollisionDetector.testCirclevsCircle(
            Vec2(0f, 0f), 1f,
            Vec2(2f, 0f), 1f
        )
        assertTrue(result.colliding)
        assertEquals(0f, result.contactPoints[0].depth, 0.001f)
    }

    @Test
    fun `test Circle vs Circle - one inside other`() {
        val result = CollisionDetector.testCirclevsCircle(
            Vec2(0f, 0f), 3f,
            Vec2(1f, 0f), 1f
        )
        assertTrue(result.colliding)
        assertTrue(result.contactPoints[0].depth > 0f)
    }

    @Test
    fun `test Circle vs Circle - concentric`() {
        val result = CollisionDetector.testCirclevsCircle(
            Vec2(0f, 0f), 2f,
            Vec2(0f, 0f), 1f
        )
        assertTrue(result.colliding)
    }

    @Test
    fun `test AABB vs Circle - overlapping`() {
        val result = CollisionDetector.testAABBvsCircle(
            Vec2(0f, 0f), Vec2(1f, 1f),
            Vec2(1.5f, 0f), 1f
        )
        assertTrue(result.colliding)
        assertTrue(result.contactPoints[0].depth > 0f)
    }

    @Test
    fun `test AABB vs Circle - not overlapping`() {
        val result = CollisionDetector.testAABBvsCircle(
            Vec2(0f, 0f), Vec2(1f, 1f),
            Vec2(5f, 0f), 1f
        )
        assertFalse(result.colliding)
    }

    @Test
    fun `test AABB vs Circle - circle inside AABB`() {
        val result = CollisionDetector.testAABBvsCircle(
            Vec2(0f, 0f), Vec2(2f, 2f),
            Vec2(0f, 0f), 0.5f
        )
        assertTrue(result.colliding)
    }

    @Test
    fun `test collision - BoxCollider vs BoxCollider`() {
        val boxA = BoxCollider2D(Vec2(1f, 1f))
        val boxB = BoxCollider2D(Vec2(1f, 1f))
        val result = CollisionDetector.testCollision(
            boxA, Vec2(0f, 0f), 0f,
            boxB, Vec2(1.5f, 0f), 0f
        )
        assertTrue(result.colliding)
    }

    @Test
    fun `test collision - CircleCollider vs CircleCollider`() {
        val circA = CircleCollider2D(1f)
        val circB = CircleCollider2D(1f)
        val result = CollisionDetector.testCollision(
            circA, Vec2(0f, 0f), 0f,
            circB, Vec2(1f, 0f), 0f
        )
        assertTrue(result.colliding)
    }

    @Test
    fun `test collision - BoxCollider vs CircleCollider`() {
        val box = BoxCollider2D(Vec2(1f, 1f))
        val circ = CircleCollider2D(1f)
        val result = CollisionDetector.testCollision(
            box, Vec2(0f, 0f), 0f,
            circ, Vec2(1.5f, 0f), 0f
        )
        assertTrue(result.colliding)
    }

    @Test
    fun `test collision - CircleCollider vs BoxCollider`() {
        val circ = CircleCollider2D(1f)
        val box = BoxCollider2D(Vec2(1f, 1f))
        val result = CollisionDetector.testCollision(
            circ, Vec2(1.5f, 0f), 0f,
            box, Vec2(0f, 0f), 0f
        )
        assertTrue(result.colliding)
    }

    @Test
    fun `test collision - non-overlapping different collider types`() {
        val box = BoxCollider2D(Vec2(1f, 1f))
        val circ = CircleCollider2D(1f)
        val result = CollisionDetector.testCollision(
            box, Vec2(0f, 0f), 0f,
            circ, Vec2(5f, 0f), 0f
        )
        assertFalse(result.colliding)
    }

    @Test
    fun `test AABB vs AABB contact normal direction`() {
        val a = AABB(Vec2(0f, 0f), Vec2(2f, 2f))
        val b = AABB(Vec2(1f, 0f), Vec2(3f, 2f))
        val result = CollisionDetector.testAABBvsAABB(a, b)
        assertTrue(result.colliding)
        val normal = result.contactPoints[0].normal
        // Normal should point from A to B
        assertTrue(normal.x > 0f || normal.y != 0f)
    }

    @Test
    fun `test Circle vs Circle contact normal points from A to B`() {
        val result = CollisionDetector.testCirclevsCircle(
            Vec2(0f, 0f), 1f,
            Vec2(1f, 0f), 1f
        )
        assertTrue(result.colliding)
        val normal = result.contactPoints[0].normal
        assertEquals(1f, normal.x, 0.001f)
        assertEquals(0f, normal.y, 0.001f)
    }

    @Test
    fun `test AABB vs Circle contact normal`() {
        val result = CollisionDetector.testAABBvsCircle(
            Vec2(0f, 0f), Vec2(1f, 1f),
            Vec2(1.5f, 0f), 1f
        )
        assertTrue(result.colliding)
        val normal = result.contactPoints[0].normal
        // Normal should point roughly right (from AABB to circle)
        assertTrue(normal.x > 0f)
    }
}
