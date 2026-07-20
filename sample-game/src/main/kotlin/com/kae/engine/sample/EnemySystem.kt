package com.kae.engine.sample

import com.kae.engine.scene.World
import com.kae.engine.scene.System
import com.kae.engine.scene.components.TransformComponent

class EnemySystem : System() {
    override val priority: Int = 2

    override fun update(world: World, deltaTime: Float) {
        world.forEach<EnemyComponent, TransformComponent> { entity, enemy, transform ->
        }
    }
}
