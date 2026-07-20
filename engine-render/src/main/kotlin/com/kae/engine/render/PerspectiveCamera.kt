package com.kae.engine.render

import com.kae.engine.math.Mat4
import com.kae.engine.math.Vec3
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

class PerspectiveCamera(
    var fov: Float,
    var aspectRatio: Float,
    nearPlane: Float = 0.1f,
    farPlane: Float = 1000f
) : Camera() {

    var yaw: Float = -90f
    var pitch: Float = 0f

    private var forward: Vec3 = Vec3.FORWARD
    private var rightVec: Vec3 = Vec3.RIGHT
    private var up: Vec3 = Vec3.UP

    init {
        near = nearPlane
        far = farPlane
        position = Vec3(0f, 0f, 3f)
        update()
    }

    override fun update() {
        val fovRad = Math.toRadians(fov.toDouble()).toFloat()
        projectionMatrix = Mat4.perspective(fovRad, aspectRatio, near, far)

        val yawRad = Math.toRadians(yaw.toDouble()).toFloat()
        val pitchRad = Math.toRadians(pitch.toDouble()).toFloat()

        forward = Vec3(
            cos(pitchRad) * cos(yawRad),
            sin(pitchRad),
            cos(pitchRad) * sin(yawRad)
        ).normalized()

        rightVec = forward.cross(Vec3.UP).normalized()
        up = rightVec.cross(forward).normalized()

        val target = position + forward
        viewMatrix = Mat4.lookAt(position, target, up)
        dirty = false
    }

    fun lookAt(target: Vec3) {
        val direction = (target - position).normalized()
        yaw = Math.toDegrees(
            kotlin.math.atan2(direction.z.toDouble(), direction.x.toDouble())
        ).toFloat()
        pitch = Math.toDegrees(
            kotlin.math.asin(direction.y.toDouble().coerceIn(-1.0, 1.0))
        ).toFloat()
        dirty = true
    }

    fun rotate(yawDelta: Float, pitchDelta: Float) {
        yaw += yawDelta
        pitch += pitchDelta
        pitch = pitch.coerceIn(-89f, 89f)
        dirty = true
    }

    fun getForward(): Vec3 {
        if (dirty) update()
        return forward
    }

    fun getRight(): Vec3 {
        if (dirty) update()
        return rightVec
    }

    fun getUp(): Vec3 {
        if (dirty) update()
        return up
    }
}
