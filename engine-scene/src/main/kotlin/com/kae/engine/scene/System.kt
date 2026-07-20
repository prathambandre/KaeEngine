package com.kae.engine.scene

abstract class System {
    open val priority: Int = 0
    var isEnabled: Boolean = true
    protected var isInitialized: Boolean = false

    open fun init(world: World) { isInitialized = true }
    abstract fun update(world: World, deltaTime: Float)
    open fun render(world: World, interpolation: Float) {}
    open fun destroy() {}
}
