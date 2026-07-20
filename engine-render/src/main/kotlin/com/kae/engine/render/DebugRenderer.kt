package com.kae.engine.render

import android.opengl.GLES30
import com.kae.engine.math.Vec3
import com.kae.engine.math.Vec4
import java.nio.ByteBuffer
import java.nio.ByteOrder

class DebugRenderer {

    private var drawing: Boolean = false
    private var camera: Camera? = null

    private var vaoId: Int = 0
    private var vboId: Int = 0

    private val maxVertices = 1024
    private val floatsPerVertex = 7
    private val vertexData = FloatArray(maxVertices * floatsPerVertex)
    private var vertexCount: Int = 0

    private var shader: Shader? = null

    private val vertexShaderSource = """
        #version 300 es
        layout(location = 0) in vec3 aPosition;
        layout(location = 1) in vec4 aColor;
        uniform mat4 uProjection;
        uniform mat4 uView;
        out vec4 vColor;
        void main() {
            vColor = aColor;
            gl_Position = uProjection * uView * vec4(aPosition, 1.0);
        }
    """.trimIndent()

    private val fragmentShaderSource = """
        #version 300 es
        precision mediump float;
        in vec4 vColor;
        out vec4 fragColor;
        void main() {
            fragColor = vColor;
        }
    """.trimIndent()

    init {
        shader = Shader(vertexShaderSource, fragmentShaderSource)

        val vaoArr = IntArray(1)
        GLES30.glGenVertexArrays(1, vaoArr, 0)
        vaoId = vaoArr[0]

        val vboArr = IntArray(1)
        GLES30.glGenBuffers(1, vboArr, 0)
        vboId = vboArr[0]

        GLES30.glBindVertexArray(vaoId)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboId)
        GLES30.glBufferData(
            GLES30.GL_ARRAY_BUFFER,
            maxVertices * floatsPerVertex * 4,
            null,
            GLES30.GL_DYNAMIC_DRAW
        )

        val stride = floatsPerVertex * 4
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, stride, 0)

        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(1, 4, GLES30.GL_FLOAT, false, stride, 3 * 4)

        GLES30.glBindVertexArray(0)
    }

    fun begin(camera: Camera) {
        this.camera = camera
        drawing = true
        vertexCount = 0
    }

    fun drawRect(x: Float, y: Float, width: Float, height: Float, color: Vec4) {
        if (!drawing) return
        val z = 0f

        addVertex(x, y, z, color)
        addVertex(x + width, y, z, color)
        addVertex(x + width, y + height, z, color)

        addVertex(x, y, z, color)
        addVertex(x + width, y + height, z, color)
        addVertex(x, y + height, z, color)
    }

    fun drawCircle(x: Float, y: Float, radius: Float, color: Vec4, segments: Int = 32) {
        if (!drawing) return
        val z = 0f
        val angleStep = (2.0 * Math.PI / segments).toFloat()

        for (i in 0 until segments) {
            val angle1 = i * angleStep
            val angle2 = (i + 1) * angleStep

            val x1 = x + kotlin.math.cos(angle1.toDouble()).toFloat() * radius
            val y1 = y + kotlin.math.sin(angle1.toDouble()).toFloat() * radius
            val x2 = x + kotlin.math.cos(angle2.toDouble()).toFloat() * radius
            val y2 = y + kotlin.math.sin(angle2.toDouble()).toFloat() * radius

            addVertex(x, y, z, color)
            addVertex(x1, y1, z, color)
            addVertex(x2, y2, z, color)
        }
    }

    fun drawLine(start: Vec3, end: Vec3, color: Vec4) {
        if (!drawing) return
        addVertex(start.x, start.y, start.z, color)
        addVertex(end.x, end.y, end.z, color)
    }

    fun drawAABB(min: Vec3, max: Vec3, color: Vec4) {
        val corners = arrayOf(
            Vec3(min.x, min.y, min.z),
            Vec3(max.x, min.y, min.z),
            Vec3(max.x, max.y, min.z),
            Vec3(min.x, max.y, min.z),
            Vec3(min.x, min.y, max.z),
            Vec3(max.x, min.y, max.z),
            Vec3(max.x, max.y, max.z),
            Vec3(min.x, max.y, max.z)
        )

        val edges = arrayOf(
            0 to 1, 1 to 2, 2 to 3, 3 to 0,
            4 to 5, 5 to 6, 6 to 7, 7 to 4,
            0 to 4, 1 to 5, 2 to 6, 3 to 7
        )

        for ((a, b) in edges) {
            drawLine(corners[a], corners[b], color)
        }
    }

    fun end() {
        if (!drawing) return
        drawing = false

        if (vertexCount == 0) return

        shader?.use()
        camera?.let { cam ->
            shader?.setMat4("uProjection", cam.computeProjectionMatrix())
            shader?.setMat4("uView", cam.computeViewMatrix())
        }

        GLES30.glBindVertexArray(vaoId)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboId)

        val buffer = ByteBuffer.allocateDirect(vertexCount * floatsPerVertex * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        buffer.put(vertexData, 0, vertexCount * floatsPerVertex)
        buffer.position(0)

        GLES30.glBufferSubData(
            GLES30.GL_ARRAY_BUFFER, 0,
            vertexCount * floatsPerVertex * 4, buffer
        )

        GLES30.glLineWidth(1f)
        GLES30.glDrawArrays(GLES30.GL_LINES, 0, vertexCount)

        GLES30.glBindVertexArray(0)
        vertexCount = 0
    }

    fun dispose() {
        shader?.destroy()
        shader = null
        val buffers = intArrayOf(vboId)
        GLES30.glDeleteBuffers(1, buffers, 0)
        val vaos = intArrayOf(vaoId)
        GLES30.glDeleteVertexArrays(1, vaos, 0)
    }

    private fun addVertex(x: Float, y: Float, z: Float, color: Vec4) {
        if (vertexCount >= maxVertices) return
        val i = vertexCount * floatsPerVertex
        vertexData[i + 0] = x
        vertexData[i + 1] = y
        vertexData[i + 2] = z
        vertexData[i + 3] = color.x
        vertexData[i + 4] = color.y
        vertexData[i + 5] = color.z
        vertexData[i + 6] = color.w
        vertexCount++
    }
}
