package com.kae.engine.physics

import com.kae.engine.math.Vec2
import com.kae.engine.scene.Entity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class QuadtreeTest {

    private lateinit var quadtree: Quadtree

    @BeforeEach
    fun setUp() {
        quadtree = Quadtree(
            bounds = AABB(Vec2(-100f, -100f), Vec2(100f, 100f)),
            maxEntities = 4,
            maxDepth = 5
        )
    }

    @Test
    fun `test insert and getAll`() {
        val entity1 = Entity(1)
        val entity2 = Entity(2)

        quadtree.insert(entity1, AABB(Vec2(10f, 10f), Vec2(20f, 20f)))
        quadtree.insert(entity2, AABB(Vec2(30f, 30f), Vec2(40f, 40f)))

        val all = quadtree.getAll()
        assertEquals(2, all.size)
    }

    @Test
    fun `test query returns overlapping entities`() {
        val entity1 = Entity(1)
        val entity2 = Entity(2)
        val entity3 = Entity(3)

        quadtree.insert(entity1, AABB(Vec2(0f, 0f), Vec2(10f, 10f)))
        quadtree.insert(entity2, AABB(Vec2(5f, 5f), Vec2(15f, 15f)))
        quadtree.insert(entity3, AABB(Vec2(50f, 50f), Vec2(60f, 60f)))

        val queryArea = AABB(Vec2(3f, 3f), Vec2(12f, 12f))
        val results = quadtree.query(queryArea)

        assertEquals(2, results.size)
        assertTrue(results.any { it.entity.id == 1 })
        assertTrue(results.any { it.entity.id == 2 })
    }

    @Test
    fun `test query no overlap`() {
        val entity = Entity(1)
        quadtree.insert(entity, AABB(Vec2(0f, 0f), Vec2(10f, 10f)))

        val queryArea = AABB(Vec2(50f, 50f), Vec2(60f, 60f))
        val results = quadtree.query(queryArea)
        assertTrue(results.isEmpty())
    }

    @Test
    fun `test queryPoint`() {
        val entity1 = Entity(1)
        val entity2 = Entity(2)

        quadtree.insert(entity1, AABB(Vec2(0f, 0f), Vec2(10f, 10f)))
        quadtree.insert(entity2, AABB(Vec2(20f, 20f), Vec2(30f, 30f)))

        val results = quadtree.queryPoint(Vec2(5f, 5f))
        assertEquals(1, results.size)
        assertEquals(1, results[0].entity.id)
    }

    @Test
    fun `test queryPoint - no match`() {
        val entity = Entity(1)
        quadtree.insert(entity, AABB(Vec2(0f, 0f), Vec2(10f, 10f)))

        val results = quadtree.queryPoint(Vec2(50f, 50f))
        assertTrue(results.isEmpty())
    }

    @Test
    fun `test remove entity`() {
        val entity1 = Entity(1)
        val entity2 = Entity(2)

        quadtree.insert(entity1, AABB(Vec2(0f, 0f), Vec2(10f, 10f)))
        quadtree.insert(entity2, AABB(Vec2(20f, 20f), Vec2(30f, 30f)))

        quadtree.remove(entity1)

        val all = quadtree.getAll()
        assertEquals(1, all.size)
        assertEquals(2, all[0].entity.id)
    }

    @Test
    fun `test clear`() {
        quadtree.insert(Entity(1), AABB(Vec2(0f, 0f), Vec2(10f, 10f)))
        quadtree.insert(Entity(2), AABB(Vec2(20f, 20f), Vec2(30f, 30f)))
        quadtree.insert(Entity(3), AABB(Vec2(40f, 40f), Vec2(50f, 50f)))

        quadtree.clear()

        assertTrue(quadtree.getAll().isEmpty())
    }

    @Test
    fun `test subdivision occurs when exceeding maxEntities`() {
        val smallQuadtree = Quadtree(
            bounds = AABB(Vec2(0f, 0f), Vec2(100f, 100f)),
            maxEntities = 2,
            maxDepth = 5
        )

        for (i in 0..5) {
            val x = (i * 10).toFloat()
            smallQuadtree.insert(Entity(i), AABB(Vec2(x, 0f), Vec2(x + 5f, 5f)))
        }

        val all = smallQuadtree.getAll()
        assertEquals(6, all.size)
    }

    @Test
    fun `test entities in different quadrants are found`() {
        val smallQuadtree = Quadtree(
            bounds = AABB(Vec2(-50f, -50f), Vec2(50f, 50f)),
            maxEntities = 2,
            maxDepth = 5
        )

        smallQuadtree.insert(Entity(1), AABB(Vec2(10f, 10f), Vec2(20f, 20f)))
        smallQuadtree.insert(Entity(2), AABB(Vec2(-20f, 10f), Vec2(-10f, 20f)))
        smallQuadtree.insert(Entity(3), AABB(Vec2(-20f, -20f), Vec2(-10f, -10f)))
        smallQuadtree.insert(Entity(4), AABB(Vec2(10f, -20f), Vec2(20f, -10f)))

        val all = smallQuadtree.getAll()
        assertEquals(4, all.size)
    }

    @Test
    fun `test query covers multiple quadrants`() {
        val smallQuadtree = Quadtree(
            bounds = AABB(Vec2(-50f, -50f), Vec2(50f, 50f)),
            maxEntities = 2,
            maxDepth = 5
        )

        smallQuadtree.insert(Entity(1), AABB(Vec2(10f, 10f), Vec2(20f, 20f)))
        smallQuadtree.insert(Entity(2), AABB(Vec2(-20f, 10f), Vec2(-10f, 20f)))
        smallQuadtree.insert(Entity(3), AABB(Vec2(-20f, -20f), Vec2(-10f, -10f)))
        smallQuadtree.insert(Entity(4), AABB(Vec2(10f, -20f), Vec2(20f, -10f)))

        val results = smallQuadtree.query(AABB(Vec2(-30f, -30f), Vec2(30f, 30f)))
        assertEquals(4, results.size)
    }

    @Test
    fun `test remove non-existent entity`() {
        quadtree.insert(Entity(1), AABB(Vec2(0f, 0f), Vec2(10f, 10f)))
        quadtree.remove(Entity(999))

        assertEquals(1, quadtree.getAll().size)
    }

    @Test
    fun `test insert entity outside bounds is ignored`() {
        quadtree.insert(Entity(1), AABB(Vec2(200f, 200f), Vec2(300f, 300f)))

        assertTrue(quadtree.getAll().isEmpty())
    }

    @Test
    fun `test duplicate insert same entity`() {
        val entity = Entity(1)
        quadtree.insert(entity, AABB(Vec2(0f, 0f), Vec2(10f, 10f)))
        quadtree.insert(entity, AABB(Vec2(0f, 0f), Vec2(10f, 10f)))

        val all = quadtree.getAll()
        assertEquals(2, all.size)
    }

    @Test
    fun `test insert and query at quadrant boundaries`() {
        val smallQuadtree = Quadtree(
            bounds = AABB(Vec2(-50f, -50f), Vec2(50f, 50f)),
            maxEntities = 2,
            maxDepth = 3
        )

        smallQuadtree.insert(Entity(1), AABB(Vec2(-5f, -5f), Vec2(5f, 5f)))

        val results = smallQuadtree.query(AABB(Vec2(0f, 0f), Vec2(1f, 1f)))
        assertEquals(1, results.size)
    }

    @Test
    fun `test many entities`() {
        for (i in 0..49) {
            val x = (i % 10) * 10f
            val y = (i / 10) * 10f
            quadtree.insert(Entity(i), AABB(Vec2(x, y), Vec2(x + 5f, y + 5f)))
        }

        val all = quadtree.getAll()
        assertEquals(50, all.size)

        val results = quadtree.query(AABB(Vec2(5f, 5f), Vec2(15f, 15f)))
        assertTrue(results.isNotEmpty())
    }

    @Test
    fun `test clear after subdivision`() {
        val smallQuadtree = Quadtree(
            bounds = AABB(Vec2(0f, 0f), Vec2(100f, 100f)),
            maxEntities = 2,
            maxDepth = 3
        )

        for (i in 0..5) {
            smallQuadtree.insert(Entity(i), AABB(Vec2(i * 10f, 0f), Vec2(i * 10f + 5f, 5f)))
        }

        smallQuadtree.clear()
        assertTrue(smallQuadtree.getAll().isEmpty())
    }
}
