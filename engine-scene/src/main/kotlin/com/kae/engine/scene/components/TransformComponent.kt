package com.kae.engine.scene.components

import com.kae.engine.math.Vec3
import com.kae.engine.math.Quaternion
import com.kae.engine.math.Mat4
import com.kae.engine.scene.Component

data class TransformComponent(
    var position: Vec3 = Vec3(0f, 0f, 0f),
    var rotation: Quaternion = Quaternion.IDENTITY,
    var scale: Vec3 = Vec3(1f, 1f, 1f)
) : Component {
    fun toMatrix(): Mat4 = com.kae.engine.math.Transform(position, rotation, scale).toMatrix()
}
