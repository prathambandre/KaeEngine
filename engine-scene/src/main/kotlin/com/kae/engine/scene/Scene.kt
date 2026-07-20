package com.kae.engine.scene

open class Scene(val name: String) {
    lateinit var world: World

    open fun load() {}
    open fun update(deltaTime: Float) { world.update(deltaTime) }
    open fun render(interpolation: Float) { world.render(interpolation) }
    open fun destroy() { world.clear() }
}
