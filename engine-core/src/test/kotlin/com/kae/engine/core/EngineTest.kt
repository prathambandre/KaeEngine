package com.kae.engine.core

import com.kae.engine.scene.System
import com.kae.engine.scene.World
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class EngineTest {

    @Test
    fun `engine starts and stops correctly`() {
        val engine = Engine()
        assertFalse(engine.isRunning)

        engine.start()
        assertTrue(engine.isRunning)

        engine.stop()
        assertFalse(engine.isRunning)
    }

    @Test
    fun `engine pause and resume`() {
        val engine = Engine()
        engine.start()

        engine.pause()
        assertFalse(engine.isRunning)

        engine.resume()
        assertTrue(engine.isRunning)
    }

    @Test
    fun `engine initialize sets config`() {
        val engine = Engine()
        val config = EngineConfig(
            targetFPS = 120,
            width = 1920,
            height = 1080,
            title = "Test Game"
        )
        engine.initialize(config)
        assertEquals(120, engine.config.targetFPS)
        assertEquals(1920, engine.config.width)
        assertEquals(1080, engine.config.height)
        assertEquals("Test Game", engine.config.title)
    }

    @Test
    fun `engine main loop calls update and render`() {
        val engine = Engine()
        engine.initialize(EngineConfig(fixedTimeStep = 1f / 60f, maxFrameSkip = 5))

        var updateCount = 0

        val testSystem = object : System() {
            override fun update(world: World, deltaTime: Float) {
                updateCount++
            }
        }

        engine.addSystem(testSystem)
        engine.start()

        engine.mainLoop(1f / 60f)

        assertTrue(updateCount > 0)
    }

    @Test
    fun `engine does not run main loop when not started`() {
        val engine = Engine()
        var updateCount = 0

        val testSystem = object : System() {
            override fun update(world: World, deltaTime: Float) {
                updateCount++
            }
        }

        engine.addSystem(testSystem)

        engine.mainLoop(1f / 60f)
        assertEquals(0, updateCount)
    }

    @Test
    fun `engine shutdown cleans up`() {
        val engine = Engine()
        val testSystem = object : System() {
            override fun update(world: World, deltaTime: Float) {}
        }

        engine.addSystem(testSystem)
        engine.start()
        engine.shutdown()

        assertFalse(engine.isRunning)
    }

    @Test
    fun `engine add and remove systems`() {
        val engine = Engine()
        val system1 = object : System() {
            override fun update(world: World, deltaTime: Float) {}
        }
        val system2 = object : System() {
            override fun update(world: World, deltaTime: Float) {}
        }

        engine.addSystem(system1)
        engine.addSystem(system2)

        assertEquals(2, engine.ecsWorld.getSystems().size)

        engine.removeSystem(system1)

        assertEquals(1, engine.ecsWorld.getSystems().size)
        assertSame(system2, engine.ecsWorld.getSystems()[0])
    }

    @Test
    fun `system priority determines update order`() {
        val engine = Engine()
        val order = mutableListOf<Int>()

        val system1 = object : System() {
            override val priority: Int = 2
            override fun update(world: World, deltaTime: Float) { order.add(2) }
        }
        val system2 = object : System() {
            override val priority: Int = 1
            override fun update(world: World, deltaTime: Float) { order.add(1) }
        }
        val system3 = object : System() {
            override val priority: Int = 3
            override fun update(world: World, deltaTime: Float) { order.add(3) }
        }

        engine.addSystem(system1)
        engine.addSystem(system2)
        engine.addSystem(system3)

        engine.start()
        engine.mainLoop(1f / 60f)

        assertEquals(listOf(1, 2, 3), order)
    }
}
