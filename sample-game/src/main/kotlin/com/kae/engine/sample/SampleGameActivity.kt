package com.kae.engine.sample

import com.kae.engine.platform.EngineActivity
import com.kae.engine.core.Engine
import com.kae.engine.core.EngineConfig
import com.kae.engine.math.Vec4
import com.kae.engine.math.Vec2
import com.kae.engine.render.RenderSystem
import com.kae.engine.physics.PhysicsSystem

class SampleGameActivity : EngineActivity() {
    override fun onInitializeEngine(config: EngineConfig): Engine {
        return Engine()
    }

    override fun onEngineCreate() {
        val gameConfig = EngineConfig(
            width = 800, height = 600,
            title = "KaeEngine - Breakout",
            targetFPS = 60,
            clearColor = Vec4(0.1f, 0.1f, 0.15f, 1f),
            enablePhysics = true,
            gravity = Vec2(0f, 0f)
        )
        engine.initialize(gameConfig)

        val renderSystem = RenderSystem()
        engine.addSystem(renderSystem)
        engine.addSystem(PhysicsSystem())
        engine.addSystem(PlayerSystem(inputManager))
        engine.addSystem(BallSystem())
        engine.addSystem(EnemySystem())

        val scene = BreakoutScene()
        sceneManager.loadScene("breakout", scene)
        sceneManager.switchTo("breakout")
    }

    override fun onUpdate(deltaTime: Float) {}
}
