package com.kae.engine.physics

import com.kae.engine.scene.Entity
import com.kae.engine.math.Vec2

data class QuadNode(
    val bounds: AABB,
    val entities: MutableList<QuadEntity> = mutableListOf(),
    val children: Array<QuadNode?> = arrayOfNulls(4)
)

data class QuadEntity(
    val entity: Entity,
    val bounds: AABB
)

class Quadtree(
    val bounds: AABB,
    val maxEntities: Int = 8,
    val maxDepth: Int = 5
) {
    private val root = QuadNode(bounds)

    fun insert(entity: Entity, bounds: AABB) {
        insertInto(root, QuadEntity(entity, bounds), 0)
    }

    fun remove(entity: Entity) {
        removeFrom(root, entity)
    }

    fun query(area: AABB): List<QuadEntity> {
        val results = mutableListOf<QuadEntity>()
        queryArea(root, area, results)
        return results
    }

    fun queryPoint(point: Vec2): List<QuadEntity> {
        val results = mutableListOf<QuadEntity>()
        queryPointRecursive(root, point, results)
        return results
    }

    fun getAll(): List<QuadEntity> {
        val results = mutableListOf<QuadEntity>()
        collectAll(root, results)
        return results
    }

    fun clear() {
        clearNode(root)
    }

    private fun insertInto(node: QuadNode, quadEntity: QuadEntity, depth: Int) {
        if (!node.bounds.intersects(quadEntity.bounds)) return

        if (node.children[0] != null) {
            val indices = getIndices(node, quadEntity.bounds)
            var inserted = false
            for (index in indices) {
                node.children[index]?.let { child ->
                    if (child.bounds.contains(quadEntity.bounds)) {
                        insertInto(child, quadEntity, depth + 1)
                        inserted = true
                    }
                }
            }
            if (inserted) return
        }

        node.entities.add(quadEntity)

        if (node.entities.size > maxEntities && depth < maxDepth && node.children[0] == null) {
            subdivide(node)
        }
    }

    private fun removeFrom(node: QuadNode, entity: Entity) {
        node.entities.removeAll { it.entity == entity }
        for (child in node.children) {
            child?.let { removeFrom(it, entity) }
        }
    }

    private fun queryArea(node: QuadNode, area: AABB, results: MutableList<QuadEntity>) {
        if (!node.bounds.intersects(area)) return
        for (entity in node.entities) {
            if (entity.bounds.intersects(area)) {
                results.add(entity)
            }
        }
        for (child in node.children) {
            child?.let { queryArea(it, area, results) }
        }
    }

    private fun queryPointRecursive(node: QuadNode, point: Vec2, results: MutableList<QuadEntity>) {
        if (!node.bounds.containsPoint(point)) return
        for (entity in node.entities) {
            if (entity.bounds.containsPoint(point)) {
                results.add(entity)
            }
        }
        for (child in node.children) {
            child?.let { queryPointRecursive(it, point, results) }
        }
    }

    private fun collectAll(node: QuadNode, results: MutableList<QuadEntity>) {
        results.addAll(node.entities)
        for (child in node.children) {
            child?.let { collectAll(it, results) }
        }
    }

    private fun clearNode(node: QuadNode) {
        node.entities.clear()
        for (i in node.children.indices) {
            node.children[i]?.let { clearNode(it); node.children[i] = null }
        }
    }

    private fun subdivide(node: QuadNode) {
        val center = node.bounds.center
        val halfSize = node.bounds.halfSize

        node.children[0] = QuadNode(AABB(
            Vec2(center.x, center.y),
            Vec2(center.x + halfSize.x, center.y + halfSize.y)
        ))
        node.children[1] = QuadNode(AABB(
            Vec2(center.x - halfSize.x, center.y),
            Vec2(center.x, center.y + halfSize.y)
        ))
        node.children[2] = QuadNode(AABB(
            Vec2(center.x - halfSize.x, center.y - halfSize.y),
            Vec2(center.x, center.y)
        ))
        node.children[3] = QuadNode(AABB(
            Vec2(center.x, center.y - halfSize.y),
            Vec2(center.x + halfSize.x, center.y)
        ))

        val existing = node.entities.toList()
        node.entities.clear()
        for (quadEntity in existing) {
            val indices = getIndices(node, quadEntity.bounds)
            var inserted = false
            for (index in indices) {
                node.children[index]?.let { child ->
                    if (child.bounds.contains(quadEntity.bounds)) {
                        child.entities.add(quadEntity)
                        inserted = true
                    }
                }
            }
            if (!inserted) {
                node.entities.add(quadEntity)
            }
        }
    }

    private fun getIndices(node: QuadNode, bounds: AABB): List<Int> {
        val center = node.bounds.center
        val indices = mutableListOf<Int>()

        val overlapsTop = bounds.max.y >= center.y
        val overlapsBottom = bounds.min.y <= center.y
        val overlapsLeft = bounds.min.x <= center.x
        val overlapsRight = bounds.max.x >= center.x

        if (overlapsTop && overlapsRight) indices.add(0)
        if (overlapsTop && overlapsLeft) indices.add(1)
        if (overlapsBottom && overlapsLeft) indices.add(2)
        if (overlapsBottom && overlapsRight) indices.add(3)

        return indices
    }
}
