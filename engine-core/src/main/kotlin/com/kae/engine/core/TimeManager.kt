package com.kae.engine.core

class TimeManager(
    fixedTimeStep: Float = 1f / 60f,
    maxFrameSkip: Int = 5
) {
    var fixedTimeStep: Float = fixedTimeStep
        set(value) { field = if (value > 0f) value else 1f / 60f }

    var maxFrameSkip: Int = maxFrameSkip
        set(value) { field = if (value > 0) value else 5 }

    var accumulatedSteps: Int = 0
        private set

    var fps: Int = 0
        private set

    var totalTime: Float = 0f
        private set

    var frameCount: Long = 0
        private set

    private var accumulator: Float = 0f
    private var fpsTimer: Float = 0f
    private var fpsFrameCount: Int = 0
    private var paused: Boolean = false
    private var pauseAccumulator: Float = 0f

    fun update(realDeltaTime: Float): Int {
        if (paused) {
            accumulatedSteps = 0
            return 0
        }

        accumulator += realDeltaTime
        var steps = 0

        while (accumulator >= fixedTimeStep && steps < maxFrameSkip) {
            accumulator -= fixedTimeStep
            steps++
        }

        accumulatedSteps = steps
        frameCount++
        fpsFrameCount++
        fpsTimer += realDeltaTime

        if (fpsTimer >= 1f) {
            fps = fpsFrameCount
            fpsFrameCount = 0
            fpsTimer -= 1f
        }

        totalTime += realDeltaTime

        return steps
    }

    fun getInterpolation(): Float {
        return if (fixedTimeStep > 0f) (accumulator / fixedTimeStep).coerceIn(0f, 1f) else 0f
    }

    fun pause() {
        if (!paused) {
            paused = true
            pauseAccumulator = accumulator
            accumulator = 0f
        }
    }

    fun resume() {
        if (paused) {
            paused = false
            accumulator = pauseAccumulator
            pauseAccumulator = 0f
        }
    }

    fun isPaused(): Boolean = paused

    fun reset() {
        accumulator = 0f
        totalTime = 0f
        frameCount = 0
        fps = 0
        fpsTimer = 0f
        fpsFrameCount = 0
        pauseAccumulator = 0f
        accumulatedSteps = 0
    }
}
