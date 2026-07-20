package com.kae.engine.core

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class TimeManagerTest {

    @Test
    fun `initial state is correct`() {
        val time = TimeManager(fixedTimeStep = 1f / 60f)
        assertEquals(0f, time.totalTime)
        assertEquals(0L, time.frameCount)
        assertEquals(0, time.fps)
        assertFalse(time.isPaused())
    }

    @Test
    fun `update accumulates and returns step count`() {
        val time = TimeManager(fixedTimeStep = 0.02f)
        val steps = time.update(0.1f)
        assertEquals(5, steps)
        assertEquals(0.1f, time.totalTime, 0.001f)
        assertEquals(1L, time.frameCount)
    }

    @Test
    fun `partial step leaves remainder in accumulator`() {
        val time = TimeManager(fixedTimeStep = 0.02f)
        val steps = time.update(0.05f)
        assertEquals(2, steps)
        assertEquals(0.05f, time.totalTime, 0.001f)
        val interp = time.getInterpolation()
        assertTrue(interp > 0f)
        assertTrue(interp < 1f)
    }

    @Test
    fun `interpolation is zero when accumulator is empty`() {
        val time = TimeManager(fixedTimeStep = 0.02f)
        time.update(0.02f)
        assertEquals(0f, time.getInterpolation(), 0.001f)
    }

    @Test
    fun `interpolation is within bounds`() {
        val time = TimeManager(fixedTimeStep = 0.02f)
        time.update(0.015f)
        val interp = time.getInterpolation()
        assertTrue(interp >= 0f)
        assertTrue(interp <= 1f)
        assertEquals(0.75f, interp, 0.01f)
    }

    @Test
    fun `max frame skip limits steps`() {
        val time = TimeManager(fixedTimeStep = 1f / 60f, maxFrameSkip = 3)
        val steps = time.update(1f)
        assertEquals(3, steps)
    }

    @Test
    fun `pause stops update accumulation`() {
        val time = TimeManager(fixedTimeStep = 0.02f)
        time.update(0.1f)
        time.pause()
        assertTrue(time.isPaused())
        val steps = time.update(0.1f)
        assertEquals(0, steps)
    }

    @Test
    fun `resume restores accumulator`() {
        val time = TimeManager(fixedTimeStep = 0.02f)
        time.update(0.05f)
        val totalBeforePause = time.totalTime
        time.pause()
        time.update(0.5f)
        time.resume()
        assertFalse(time.isPaused())
        val steps = time.update(0f)
        assertEquals(0, steps)
        assertEquals(totalBeforePause, time.totalTime, 0.001f)
    }

    @Test
    fun `multiple updates accumulate total time`() {
        val time = TimeManager(fixedTimeStep = 0.02f)
        time.update(0.1f)
        time.update(0.2f)
        time.update(0.3f)
        assertEquals(0.6f, time.totalTime, 0.001f)
        assertEquals(3L, time.frameCount)
    }

    @Test
    fun `fixed delta time matches configured value`() {
        val time = TimeManager(fixedTimeStep = 1f / 30f)
        assertEquals(1f / 30f, time.fixedTimeStep, 0.0001f)
    }

    @Test
    fun `reset clears all state`() {
        val time = TimeManager(fixedTimeStep = 0.02f)
        time.update(0.5f)
        time.reset()
        assertEquals(0f, time.totalTime)
        assertEquals(0L, time.frameCount)
        assertEquals(0, time.fps)
    }

    @Test
    fun `accumulatedSteps is set after update`() {
        val time = TimeManager(fixedTimeStep = 0.02f)
        assertEquals(0, time.accumulatedSteps)
        time.update(0.07f)
        assertEquals(3, time.accumulatedSteps)
    }

    @Test
    fun `fixedTimeStep can be set`() {
        val time = TimeManager()
        time.fixedTimeStep = 0.05f
        assertEquals(0.05f, time.fixedTimeStep)
        val steps = time.update(0.15f)
        assertEquals(3, steps)
    }

    @Test
    fun `maxFrameSkip can be set`() {
        val time = TimeManager()
        time.maxFrameSkip = 2
        assertEquals(2, time.maxFrameSkip)
        val steps = time.update(1f)
        assertEquals(2, steps)
    }
}
