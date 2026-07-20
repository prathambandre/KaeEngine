package com.kae.engine.sample

import com.kae.engine.scene.Component
import com.kae.engine.math.Vec2

class PaddleComponent : Component {
    var speed: Float = 600f
    var width: Float = 120f
}

class BallComponent : Component {
    var speed: Float = 400f
    var direction: Vec2 = Vec2(1f, 1f).normalized()
    var maxSpeed: Float = 800f
}

class BrickComponent : Component {
    var points: Int = 10
    var health: Int = 1
}

class WallComponent : Component

class EnemyComponent : Component {
    var health: Int = 1
    var speed: Float = 100f
}

class PlayerComponent : Component {
    var health: Int = 3
    var score: Int = 0
}
