package com.kae.engine.render

import com.kae.engine.math.Vec4
import com.kae.engine.scene.Component

data class Material(
    var shader: Shader? = null,
    var texture: Texture? = null,
    var color: Vec4 = Vec4(1f, 1f, 1f, 1f)
)

data class SpriteComponent(
    var texture: Texture? = null,
    var region: SpriteRegion? = null,
    var color: Vec4 = Vec4(1f, 1f, 1f, 1f),
    var width: Float = 32f,
    var height: Float = 32f,
    var rotation: Float = 0f,
    var layer: Int = 0,
    var flipX: Boolean = false,
    var flipY: Boolean = false
) : Component

data class MeshComponent(
    var mesh: Mesh? = null,
    var material: Material? = null
) : Component

data class CameraComponent(
    var camera: Camera? = null,
    var isActive: Boolean = true
) : Component
