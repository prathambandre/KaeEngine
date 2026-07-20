package com.kae.engine.render

import android.opengl.GLES30
import com.kae.engine.math.Mat4
import com.kae.engine.math.Vec2
import com.kae.engine.math.Vec3
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MeshRenderer {

    fun render(mesh: Mesh, shader: Shader, camera: Camera) {
        shader.use()
        shader.setMat4("uProjection", camera.computeProjectionMatrix())
        shader.setMat4("uView", camera.computeViewMatrix())

        mesh.bind()
        GLES30.glDrawElements(
            GLES30.GL_TRIANGLES,
            mesh.indexCount,
            GLES30.GL_UNSIGNED_SHORT,
            0
        )
        mesh.unbind()
    }

    fun render(mesh: Mesh, shader: Shader, modelMatrix: Mat4, camera: Camera) {
        shader.use()
        shader.setMat4("uProjection", camera.computeProjectionMatrix())
        shader.setMat4("uView", camera.computeViewMatrix())
        shader.setMat4("uModel", modelMatrix)

        mesh.bind()
        GLES30.glDrawElements(
            GLES30.GL_TRIANGLES,
            mesh.indexCount,
            GLES30.GL_UNSIGNED_SHORT,
            0
        )
        mesh.unbind()
    }

    fun renderInstanced(mesh: Mesh, shader: Shader, camera: Camera, transforms: List<Mat4>) {
        if (transforms.isEmpty()) return

        shader.use()
        shader.setMat4("uProjection", camera.computeProjectionMatrix())
        shader.setMat4("uView", camera.computeViewMatrix())

        mesh.bind()

        for ((index, transform) in transforms.withIndex()) {
            shader.setMat4("uModel", transform)
            GLES30.glDrawElements(
                GLES30.GL_TRIANGLES,
                mesh.indexCount,
                GLES30.GL_UNSIGNED_SHORT,
                0
            )
        }

        mesh.unbind()
    }
}
