package com.kae.engine.render

import com.kae.engine.math.Mat4
import com.kae.engine.math.Vec3

class OrthographicCamera(
    viewportWidth: Float,
    viewportHeight: Float
) : Camera() {

    var left: Float = -viewportWidth / 2f
    var right: Float = viewportWidth / 2f
    var bottom: Float = -viewportHeight / 2f
    var top: Float = viewportHeight / 2f

    init {
        near = -1f
        far = 1f
        update()
    }

    override fun update() {
        val halfW = (right - left) / (2f * zoom)
        val halfH = (top - bottom) / (2f * zoom)

        viewMatrix = Mat4.translation(-position.x, -position.y, -position.z)
        projectionMatrix = Mat4.ortho(
            -halfW, halfW,
            -halfH, halfH,
            near, far
        )
        dirty = false
    }

    fun setViewport(width: Float, height: Float) {
        val cx = (left + right) / 2f
        val cy = (bottom + top) / 2f
        left = cx - width / 2f
        right = cx + width / 2f
        bottom = cy - height / 2f
        top = cy + height / 2f
        dirty = true
    }

    fun zoomBy(factor: Float) {
        zoom *= factor
        if (zoom < 0.01f) zoom = 0.01f
        dirty = true
    }

    fun zoomTo(z: Float) {
        zoom = if (z > 0f) z else 0.01f
        dirty = true
    }
}
