package com.kae.engine.scene

class SceneManager {
    private val scenes = mutableMapOf<String, Scene>()
    private var currentScene: Scene? = null
    var world: World = World()

    fun loadScene(name: String, scene: Scene) {
        scenes[name] = scene
    }

    fun switchTo(sceneName: String) {
        currentScene?.destroy()
        currentScene = scenes[sceneName]
        currentScene?.let {
            it.world = world
            it.load()
        }
    }

    fun getCurrentScene(): Scene? = currentScene

    fun update(deltaTime: Float) { currentScene?.update(deltaTime) }
    fun render(interpolation: Float) { currentScene?.render(interpolation) }
}
