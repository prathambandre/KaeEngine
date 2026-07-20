package com.kae.engine.core

import com.kae.engine.scene.World
import com.kae.engine.scene.System
import com.kae.engine.scene.SceneManager
import com.kae.engine.math.Vec4
import com.kae.engine.math.Vec2

data class EngineConfig(
    val targetFPS: Int = 60,
    val fixedTimeStep: Float = 1f / 60f,
    val width: Int = 800,
    val height: Int = 600,
    val title: String = "KaeEngine",
    val clearColor: Vec4 = Vec4(0f, 0f, 0f, 1f),
    val enablePhysics: Boolean = true,
    val gravity: Vec2 = Vec2(0f, -9.81f),
    val maxFrameSkip: Int = 5
)

class Engine {
    val ecsWorld = World()
    val time = TimeManager()
    val sceneManager = SceneManager()
    var isRunning: Boolean = false
        private set
    var config: EngineConfig = EngineConfig()
        private set
    var renderer: RendererInterface? = null

    private val systems = mutableListOf<System>()

    fun initialize(config: EngineConfig) {
        this.config = config
        time.fixedTimeStep = config.fixedTimeStep
        time.maxFrameSkip = config.maxFrameSkip
        sceneManager.world = ecsWorld
        Logger.instance.info("Engine", "Engine initialized: ${config.width}x${config.height}, target ${config.targetFPS} FPS")
    }

    fun start() {
        isRunning = true
        Logger.instance.info("Engine", "Engine started")
    }

    fun stop() {
        isRunning = false
        Logger.instance.info("Engine", "Engine stopped")
    }

    fun pause() {
        isRunning = false
        Logger.instance.info("Engine", "Engine paused")
    }

    fun resume() {
        isRunning = true
        Logger.instance.info("Engine", "Engine resumed")
    }

    fun mainLoop(deltaTime: Float) {
        if (!isRunning) return

        time.update(deltaTime)
        val fixedSteps = time.accumulatedSteps

        for (i in 0 until fixedSteps) {
            ecsWorld.update(config.fixedTimeStep)
        }

        val interpolation = time.getInterpolation()
        renderer?.beginFrame()
        ecsWorld.render(interpolation)
        renderer?.endFrame()
    }

    fun addSystem(system: System) {
        systems.add(system)
        ecsWorld.addSystem(system)
    }

    fun removeSystem(system: System) {
        systems.remove(system)
        ecsWorld.removeSystem(system)
    }

    fun shutdown() {
        isRunning = false
        ecsWorld.clear()
        systems.clear()
        renderer?.destroy()
        Logger.instance.info("Engine", "Engine shut down")
    }
}
