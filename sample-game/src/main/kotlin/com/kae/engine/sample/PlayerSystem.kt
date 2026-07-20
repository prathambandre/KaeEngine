package com.kae.engine.sample

import com.kae.engine.scene.World
import com.kae.engine.scene.System
import com.kae.engine.scene.components.TransformComponent
import com.kae.engine.input.InputManager
import com.kae.engine.math.MathUtils
import com.kae.engine.math.Vec3

class PlayerSystem(private val inputManager: InputManager) : System() {
    override val priority: Int = 0
    private val screenWidth = 800f

    override fun update(world: World, deltaTime: Float) {
        world.forEach<PaddleComponent, TransformComponent> { entity, paddle, transform ->
            if (inputManager.isTouching(0)) {
                val touchPos = inputManager.getTouchPosition(0)
                val halfWidth = paddle.width / 2f
                val clampedX = MathUtils.clamp(touchPos.x, halfWidth, screenWidth - halfWidth)
                transform.position = Vec3(clampedX, transform.position.y, 0f)
            }
        }
    }
}
