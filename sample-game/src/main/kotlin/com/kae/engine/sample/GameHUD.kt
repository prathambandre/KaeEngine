package com.kae.engine.sample

import com.kae.engine.render.DebugRenderer

class GameHUD {
    var score: Int = 0
    var lives: Int = 3
    var fps: Int = 0
    var entityCount: Int = 0
    var showDebug: Boolean = false

    fun render(debugRenderer: DebugRenderer) {
        // In a real implementation, this would use a text renderer
        // For now, it stores data for the debug overlay
    }
}
