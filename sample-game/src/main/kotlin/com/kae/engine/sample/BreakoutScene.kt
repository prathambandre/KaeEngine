package com.kae.engine.sample

import com.kae.engine.math.Vec2
import com.kae.engine.math.Vec3
import com.kae.engine.math.Vec4
import com.kae.engine.physics.BodyType
import com.kae.engine.physics.BoxCollider2D
import com.kae.engine.physics.CircleCollider2D
import com.kae.engine.physics.RigidBody2D
import com.kae.engine.render.SpriteComponent
import com.kae.engine.scene.Entity
import com.kae.engine.scene.Scene
import com.kae.engine.scene.components.TransformComponent

class BreakoutScene : Scene("breakout") {

    private lateinit var paddleEntity: Entity
    private lateinit var ballEntity: Entity
    private val brickEntities = mutableListOf<Entity>()

    var score: Int = 0
    var lives: Int = 3

    private var ballAttached: Boolean = true

    override fun load() {
        createPaddle()
        createBall()
        createBrickGrid(10, 5)
        createWalls()
    }

    private fun createPaddle() {
        paddleEntity = world.createEntity()
        world.addComponent(paddleEntity, TransformComponent(position = Vec3(400f, 80f, 0f)))
        world.addComponent(paddleEntity, SpriteComponent().apply {
            width = 120f; height = 20f; color = Vec4(0.2f, 0.6f, 1f, 1f)
        })
        world.addComponent(paddleEntity, BoxCollider2D(halfExtents = Vec2(60f, 10f)))
        world.addComponent(paddleEntity, RigidBody2D(bodyType = BodyType.KINEMATIC))
        world.addComponent(paddleEntity, PaddleComponent())
    }

    private fun createBall() {
        ballEntity = world.createEntity()
        world.addComponent(ballEntity, TransformComponent(position = Vec3(400f, 100f, 0f)))
        world.addComponent(ballEntity, SpriteComponent().apply {
            width = 16f; height = 16f; color = Vec4(1f, 1f, 1f, 1f)
        })
        world.addComponent(ballEntity, CircleCollider2D(radius = 8f))
        world.addComponent(ballEntity, RigidBody2D(
            bodyType = BodyType.DYNAMIC,
            restitution = 1f,
            friction = 0f
        ).apply { gravityScale = 0f })
        world.addComponent(ballEntity, BallComponent().apply {
            speed = 400f; direction = Vec2(1f, 1f).normalized()
        })
    }

    private fun createBrickGrid(columns: Int, rows: Int) {
        val brickWidth = 70f
        val brickHeight = 25f
        val padding = 5f
        val startX = (800f - columns * (brickWidth + padding)) / 2f + brickWidth / 2f
        val startY = 500f

        val colors = arrayOf(
            Vec4(1f, 0.3f, 0.3f, 1f),
            Vec4(1f, 0.6f, 0.2f, 1f),
            Vec4(1f, 1f, 0.3f, 1f),
            Vec4(0.3f, 1f, 0.3f, 1f),
            Vec4(0.3f, 0.5f, 1f, 1f)
        )

        for (row in 0 until rows) {
            for (col in 0 until columns) {
                val brick = world.createEntity()
                val x = startX + col * (brickWidth + padding)
                val y = startY - row * (brickHeight + padding)

                world.addComponent(brick, TransformComponent(position = Vec3(x, y, 0f)))
                world.addComponent(brick, SpriteComponent().apply {
                    width = brickWidth; height = brickHeight
                    color = colors[row % colors.size]
                })
                world.addComponent(brick, BoxCollider2D(halfExtents = Vec2(brickWidth / 2f, brickHeight / 2f)))
                world.addComponent(brick, RigidBody2D(bodyType = BodyType.STATIC, restitution = 1f))
                world.addComponent(brick, BrickComponent().apply { points = (rows - row) * 10 })
                brickEntities.add(brick)
            }
        }
    }

    private fun createWalls() {
        val wallDefs = listOf(
            Triple(400f, -10f, 800f) to 20f,
            Triple(400f, 610f, 800f) to 20f,
            Triple(-10f, 300f, 20f) to 600f,
            Triple(810f, 300f, 20f) to 600f
        )

        for ((pos, height) in wallDefs) {
            val (x, y, width) = pos
            val wall = world.createEntity()
            world.addComponent(wall, TransformComponent(position = Vec3(x, y, 0f)))
            world.addComponent(wall, BoxCollider2D(halfExtents = Vec2(width / 2f, height / 2f)))
            world.addComponent(wall, RigidBody2D(bodyType = BodyType.STATIC, restitution = 1f))
            world.addComponent(wall, WallComponent())
        }
    }

    fun resetBall() {
        val transform = world.getComponent<TransformComponent>(ballEntity) ?: return
        val ball = world.getComponent<BallComponent>(ballEntity) ?: return
        val rigidBody = world.getComponent<RigidBody2D>(ballEntity)

        transform.position = Vec3(400f, 100f, 0f)
        ball.direction = Vec2(1f, 1f).normalized()
        rigidBody?.velocity = Vec2.ZERO
        ballAttached = true
    }

    fun isBallAttached(): Boolean = ballAttached

    fun setBallAttached(attached: Boolean) {
        ballAttached = attached
    }

    fun getPaddleEntity(): Entity = paddleEntity
    fun getBallEntity(): Entity = ballEntity
    fun getBrickEntities(): List<Entity> = brickEntities
}
