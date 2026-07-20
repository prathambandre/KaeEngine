package com.kae.engine.sample

import com.kae.engine.scene.World
import com.kae.engine.scene.System
import com.kae.engine.scene.components.TransformComponent
import com.kae.engine.math.Vec2
import com.kae.engine.math.Vec3

class BallSystem : System() {
    override val priority: Int = 1

    override fun update(world: World, deltaTime: Float) {
        world.forEach<BallComponent, TransformComponent> { entity, ball, transform ->
            val movement = ball.direction * ball.speed * deltaTime
            transform.position = Vec3(
                transform.position.x + movement.x,
                transform.position.y + movement.y,
                0f
            )

            if (transform.position.x <= 8f || transform.position.x >= 792f) {
                ball.direction = Vec2(-ball.direction.x, ball.direction.y)
            }
            if (transform.position.y >= 592f) {
                ball.direction = Vec2(ball.direction.x, -ball.direction.y)
            }
            if (transform.position.y < -20f) {
                transform.position = Vec3(400f, 100f, 0f)
                ball.direction = Vec2(1f, 1f).normalized()
            }
        }
    }
}
