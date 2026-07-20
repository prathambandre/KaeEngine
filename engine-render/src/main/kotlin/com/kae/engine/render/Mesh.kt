package com.kae.engine.render

import android.opengl.GLES30
import com.kae.engine.math.Vec2
import com.kae.engine.math.Vec3
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

data class Mesh(
    val vertices: FloatArray,
    val indices: ShortArray,
    val vertexCount: Int,
    val indexCount: Int
) {
    var vaoId: Int = 0
        internal set

    var vboId: Int = 0
        internal set

    var eboId: Int = 0
        internal set

    private var uploaded: Boolean = false

    fun upload() {
        if (uploaded) return

        val vaoArr = IntArray(1)
        GLES30.glGenVertexArrays(1, vaoArr, 0)
        vaoId = vaoArr[0]

        val buffers = IntArray(2)
        GLES30.glGenBuffers(2, buffers, 0)
        vboId = buffers[0]
        eboId = buffers[1]

        GLES30.glBindVertexArray(vaoId)

        val vertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertices)
        vertexBuffer.position(0)

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboId)
        GLES30.glBufferData(
            GLES30.GL_ARRAY_BUFFER,
            vertices.size * 4,
            vertexBuffer,
            GLES30.GL_STATIC_DRAW
        )

        val indexBuffer: ShortBuffer = ByteBuffer.allocateDirect(indices.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .put(indices)
        indexBuffer.position(0)

        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, eboId)
        GLES30.glBufferData(
            GLES30.GL_ELEMENT_ARRAY_BUFFER,
            indices.size * 2,
            indexBuffer,
            GLES30.GL_STATIC_DRAW
        )

        val stride = 8 * 4

        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, stride, 0)

        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(1, 3, GLES30.GL_FLOAT, false, stride, 3 * 4)

        GLES30.glEnableVertexAttribArray(2)
        GLES30.glVertexAttribPointer(2, 2, GLES30.GL_FLOAT, false, stride, 6 * 4)

        GLES30.glBindVertexArray(0)

        uploaded = true
    }

    fun bind() {
        if (!uploaded) upload()
        GLES30.glBindVertexArray(vaoId)
    }

    fun unbind() {
        GLES30.glBindVertexArray(0)
    }

    fun dispose() {
        if (!uploaded) return
        val buffers = intArrayOf(vboId, eboId)
        GLES30.glDeleteBuffers(2, buffers, 0)
        val vaos = intArrayOf(vaoId)
        GLES30.glDeleteVertexArrays(1, vaos, 0)
        vaoId = 0
        vboId = 0
        eboId = 0
        uploaded = false
    }

    companion object {
        fun createQuad(): Mesh {
            val vertices = floatArrayOf(
                -0.5f, -0.5f, 0f,  0f, 0f, 1f,  0f, 0f,
                 0.5f, -0.5f, 0f,  0f, 0f, 1f,  1f, 0f,
                 0.5f,  0.5f, 0f,  0f, 0f, 1f,  1f, 1f,
                -0.5f,  0.5f, 0f,  0f, 0f, 1f,  0f, 1f
            )
            val indices = shortArrayOf(0, 1, 2, 2, 3, 0)
            return Mesh(vertices, indices, 4, 6)
        }

        fun createPlane(size: Float = 1f): Mesh {
            val h = size / 2f
            val vertices = floatArrayOf(
                -h, 0f, -h,  0f, 1f, 0f,  0f, 0f,
                 h, 0f, -h,  0f, 1f, 0f,  1f, 0f,
                 h, 0f,  h,  0f, 1f, 0f,  1f, 1f,
                -h, 0f,  h,  0f, 1f, 0f,  0f, 1f
            )
            val indices = shortArrayOf(0, 2, 1, 0, 3, 2)
            return Mesh(vertices, indices, 4, 6)
        }

        fun createCube(): Mesh {
            val s = 0.5f
            val vertices = floatArrayOf(
                // Front face
                -s, -s,  s,  0f, 0f, 1f,  0f, 0f,
                 s, -s,  s,  0f, 0f, 1f,  1f, 0f,
                 s,  s,  s,  0f, 0f, 1f,  1f, 1f,
                -s,  s,  s,  0f, 0f, 1f,  0f, 1f,
                // Back face
                 s, -s, -s,  0f, 0f,-1f,  0f, 0f,
                -s, -s, -s,  0f, 0f,-1f,  1f, 0f,
                -s,  s, -s,  0f, 0f,-1f,  1f, 1f,
                 s,  s, -s,  0f, 0f,-1f,  0f, 1f,
                // Top face
                -s,  s,  s,  0f, 1f, 0f,  0f, 0f,
                 s,  s,  s,  0f, 1f, 0f,  1f, 0f,
                 s,  s, -s,  0f, 1f, 0f,  1f, 1f,
                -s,  s, -s,  0f, 1f, 0f,  0f, 1f,
                // Bottom face
                -s, -s, -s,  0f,-1f, 0f,  0f, 0f,
                 s, -s, -s,  0f,-1f, 0f,  1f, 0f,
                 s, -s,  s,  0f,-1f, 0f,  1f, 1f,
                -s, -s,  s,  0f,-1f, 0f,  0f, 1f,
                // Right face
                 s, -s,  s,  1f, 0f, 0f,  0f, 0f,
                 s, -s, -s,  1f, 0f, 0f,  1f, 0f,
                 s,  s, -s,  1f, 0f, 0f,  1f, 1f,
                 s,  s,  s,  1f, 0f, 0f,  0f, 1f,
                // Left face
                -s, -s, -s, -1f, 0f, 0f,  0f, 0f,
                -s, -s,  s, -1f, 0f, 0f,  1f, 0f,
                -s,  s,  s, -1f, 0f, 0f,  1f, 1f,
                -s,  s, -s, -1f, 0f, 0f,  0f, 1f
            )
            val indices = shortArrayOf(
                0, 1, 2, 2, 3, 0,
                4, 5, 6, 6, 7, 4,
                8, 9, 10, 10, 11, 8,
                12, 13, 14, 14, 15, 12,
                16, 17, 18, 18, 19, 16,
                20, 21, 22, 22, 23, 20
            )
            return Mesh(vertices, indices, 24, 36)
        }
    }
}
