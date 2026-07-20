package com.kae.engine.render

import com.kae.engine.scene.World
import com.kae.engine.scene.System
import com.kae.engine.scene.Entity
import com.kae.engine.scene.components.TransformComponent

class RenderSystem : System() {
    override val priority: Int = 100

    private var spriteBatch: SpriteBatch? = null
    private var camera: OrthographicCamera? = null

    fun setCamera(camera: OrthographicCamera) {
        this.camera = camera
    }

    override fun init(world: World) {
        spriteBatch = SpriteBatch()
        isInitialized = true
    }

    override fun update(world: World, deltaTime: Float) {
    }

    override fun render(world: World, interpolation: Float) {
        val cam = camera ?: return
        val batch = spriteBatch ?: return

        batch.begin(cam)

        val entities = world.getEntitiesWithAB<SpriteComponent, TransformComponent>()

        for (entity in entities) {
            val sprite = world.getComponent<SpriteComponent>(entity) ?: continue
            val transform = world.getComponent<TransformComponent>(entity) ?: continue

            val texture = sprite.texture ?: continue

            if (sprite.region != null) {
                batch.draw(
                    texture, sprite.region!!,
                    transform.position.x - sprite.width / 2f,
                    transform.position.y - sprite.height / 2f,
                    sprite.width, sprite.height,
                    sprite.color, sprite.rotation,
                    0.5f, 0.5f, sprite.flipX, sprite.flipY
                )
            } else {
                batch.draw(
                    texture,
                    transform.position.x - sprite.width / 2f,
                    transform.position.y - sprite.height / 2f,
                    sprite.width, sprite.height,
                    0, 0, texture.width, texture.height,
                    sprite.color, sprite.rotation,
                    0.5f, 0.5f, sprite.flipX, sprite.flipY
                )
            }
        }

        batch.end()
    }

    override fun destroy() {
        spriteBatch?.dispose()
        spriteBatch = null
        isInitialized = false
    }
}
