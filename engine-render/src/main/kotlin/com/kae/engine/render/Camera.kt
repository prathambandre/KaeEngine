package com.kae.engine.render

import com.kae.engine.math.Mat4
import com.kae.engine.math.Vec2
import com.kae.engine.math.Vec3
import com.kae.engine.math.Vec4

abstract class Camera {

    var position: Vec3 = Vec3.ZERO
    var zoom: Float = 1f
    var near: Float = 0.1f
    var far: Float = 1000f

    protected var viewMatrix: Mat4 = Mat4.identity()
    protected var projectionMatrix: Mat4 = Mat4.identity()
    protected var dirty: Boolean = true

    abstract fun update()

    fun computeViewMatrix(): Mat4 {
        if (dirty) update()
        return viewMatrix
    }

    fun computeProjectionMatrix(): Mat4 {
        if (dirty) update()
        return projectionMatrix
    }

    fun getViewProjectionMatrix(): Mat4 {
        if (dirty) update()
        return projectionMatrix * viewMatrix
    }

    fun screenToWorld(
        screenX: Float,
        screenY: Float,
        viewportWidth: Int,
        viewportHeight: Int
    ): Vec3 {
        if (dirty) update()
        val invVP = getViewProjectionMatrix().inverse()

        val ndcX = (2f * screenX / viewportWidth) - 1f
        val ndcY = 1f - (2f * screenY / viewportHeight)

        val nearPoint = invVP.transform(Vec4(ndcX, ndcY, -1f, 1f))
        val farPoint = invVP.transform(Vec4(ndcX, ndcY, 1f, 1f))

        val nearVec = Vec3(nearPoint.x / nearPoint.w, nearPoint.y / nearPoint.w, nearPoint.z / nearPoint.w)
        val farVec = Vec3(farPoint.x / farPoint.w, farPoint.y / farPoint.w, farPoint.z / farPoint.w)

        val dir = (farVec - nearVec).normalized()
        return nearVec + dir * 0f
    }

    fun worldToScreen(worldPos: Vec3, viewportWidth: Int, viewportHeight: Int): Vec2 {
        if (dirty) update()
        val vp = getViewProjectionMatrix()
        val clipPos = vp.transform(Vec4(worldPos.x, worldPos.y, worldPos.z, 1f))

        if (kotlin.math.abs(clipPos.w) < 1e-6f) return Vec2.ZERO

        val ndcX = clipPos.x / clipPos.w
        val ndcY = clipPos.y / clipPos.w

        val screenX = ((ndcX + 1f) / 2f) * viewportWidth
        val screenY = ((1f - ndcY) / 2f) * viewportHeight

        return Vec2(screenX, screenY)
    }
}
