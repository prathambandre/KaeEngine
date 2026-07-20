package com.kae.engine.render

import android.opengl.GLES30
import com.kae.engine.math.Vec4
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer

class SpriteBatch(private val maxSprites: Int = 10000) {

    private val batch = RenderBatch(maxSprites)

    private var vaoId: Int = 0
    private var vboId: Int = 0
    private var eboId: Int = 0
    private var indexBuffer: ShortBuffer? = null

    private var currentTexture: Texture? = null
    private var camera: OrthographicCamera? = null
    private var drawing: Boolean = false
    private var shader: Shader? = null

    private val defaultVertexShader = """
        #version 300 es
        layout(location = 0) in vec2 aPosition;
        layout(location = 1) in vec2 aTexCoord;
        layout(location = 2) in vec4 aColor;
        uniform mat4 uProjection;
        uniform mat4 uView;
        out vec2 vTexCoord;
        out vec4 vColor;
        void main() {
            vTexCoord = aTexCoord;
            vColor = aColor;
            gl_Position = uProjection * uView * vec4(aPosition, 0.0, 1.0);
        }
    """.trimIndent()

    private val defaultFragmentShader = """
        #version 300 es
        precision mediump float;
        in vec2 vTexCoord;
        in vec4 vColor;
        uniform sampler2D uTexture;
        out vec4 fragColor;
        void main() {
            fragColor = texture(uTexture, vTexCoord) * vColor;
        }
    """.trimIndent()

    init {
        shader = Shader(defaultVertexShader, defaultFragmentShader)
        setupBuffers()
    }

    private fun setupBuffers() {
        val vaoArr = IntArray(1)
        GLES30.glGenVertexArrays(1, vaoArr, 0)
        vaoId = vaoArr[0]

        val buffers = IntArray(2)
        GLES30.glGenBuffers(2, buffers, 0)
        vboId = buffers[0]
        eboId = buffers[1]

        GLES30.glBindVertexArray(vaoId)

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboId)
        GLES30.glBufferData(
            GLES30.GL_ARRAY_BUFFER,
            RenderBatch.MAX_VERTICES * RenderBatch.BYTES_PER_VERTEX,
            null,
            GLES30.GL_DYNAMIC_DRAW
        )

        val indices = generateIndices(maxSprites)
        indexBuffer = ByteBuffer.allocateDirect(indices.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .put(indices)
        indexBuffer?.position(0)

        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, eboId)
        GLES30.glBufferData(
            GLES30.GL_ELEMENT_ARRAY_BUFFER,
            indices.size * 2,
            indexBuffer,
            GLES30.GL_STATIC_DRAW
        )

        val stride = RenderBatch.BYTES_PER_VERTEX

        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, stride, 0)

        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, stride, 2 * 4)

        GLES30.glEnableVertexAttribArray(2)
        GLES30.glVertexAttribPointer(2, 4, GLES30.GL_FLOAT, false, stride, 4 * 4)

        GLES30.glBindVertexArray(0)
    }

    private fun generateIndices(maxSprites: Int): ShortArray {
        val indices = ShortArray(maxSprites * 6)
        var offset = 0
        for (i in 0 until maxSprites) {
            indices[i * 6 + 0] = (offset + 0).toShort()
            indices[i * 6 + 1] = (offset + 1).toShort()
            indices[i * 6 + 2] = (offset + 2).toShort()
            indices[i * 6 + 3] = (offset + 2).toShort()
            indices[i * 6 + 4] = (offset + 3).toShort()
            indices[i * 6 + 5] = (offset + 0).toShort()
            offset += 4
        }
        return indices
    }

    fun begin(camera: OrthographicCamera) {
        if (drawing) throw IllegalStateException("SpriteBatch.begin() called while already drawing")
        this.camera = camera
        drawing = true
        batch.reset()
        currentTexture = null
    }

    fun draw(
        texture: Texture,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        srcX: Int = 0,
        srcY: Int = 0,
        srcWidth: Int = texture.width,
        srcHeight: Int = texture.height,
        color: Vec4 = Vec4(1f, 1f, 1f, 1f),
        rotation: Float = 0f,
        originX: Float = 0.5f,
        originY: Float = 0.5f,
        flipX: Boolean = false,
        flipY: Boolean = false
    ) {
        check(drawing) { "SpriteBatch not started. Call begin() first." }

        if (currentTexture != null && currentTexture != texture) {
            flush()
        }
        currentTexture = texture

        var u = srcX.toFloat() / texture.width
        var v = srcY.toFloat() / texture.height
        var u2 = (srcX + srcWidth).toFloat() / texture.width
        var v2 = (srcY + srcHeight).toFloat() / texture.height

        if (flipX) { val tmp = u; u = u2; u2 = tmp }
        if (flipY) { val tmp = v; v = v2; v2 = tmp }

        if (batch.isFull()) {
            flush()
        }

        if (rotation != 0f) {
            batch.addRotatedSprite(
                x, y, width, height,
                u, v, u2, v2,
                color.x, color.y, color.z, color.w,
                rotation, originX, originY
            )
        } else {
            batch.addSprite(
                x, y, width, height,
                u, v, u2, v2,
                color.x, color.y, color.z, color.w
            )
        }
    }

    fun draw(
        texture: Texture,
        region: SpriteRegion,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        color: Vec4 = Vec4(1f, 1f, 1f, 1f),
        rotation: Float = 0f,
        originX: Float = 0.5f,
        originY: Float = 0.5f,
        flipX: Boolean = false,
        flipY: Boolean = false
    ) {
        var u = region.u
        var v = region.v
        var u2 = region.u2
        var v2 = region.v2

        if (flipX) { val tmp = u; u = u2; u2 = tmp }
        if (flipY) { val tmp = v; v = v2; v2 = tmp }

        check(drawing) { "SpriteBatch not started. Call begin() first." }

        if (currentTexture != null && currentTexture != texture) {
            flush()
        }
        currentTexture = texture

        if (batch.isFull()) {
            flush()
        }

        if (rotation != 0f) {
            batch.addRotatedSprite(
                x, y, width, height,
                u, v, u2, v2,
                color.x, color.y, color.z, color.w,
                rotation, originX, originY
            )
        } else {
            batch.addSprite(
                x, y, width, height,
                u, v, u2, v2,
                color.x, color.y, color.z, color.w
            )
        }
    }

    fun end() {
        check(drawing) { "SpriteBatch not started. Call begin() first." }
        if (batch.getSpriteCount() > 0) {
            flush()
        }
        drawing = false
        currentTexture = null
    }

    private fun flush() {
        val spriteCount = batch.getSpriteCount()
        if (spriteCount == 0) return

        val texture = currentTexture ?: return

        shader?.use()
        camera?.let { cam ->
            shader?.setMat4("uProjection", cam.computeProjectionMatrix())
            shader?.setMat4("uView", cam.computeViewMatrix())
        }

        texture.bind(0)
        shader?.setInt("uTexture", 0)

        GLES30.glBindVertexArray(vaoId)

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboId)
        val vertexData = batch.getVertices()
        val floatCount = batch.getFloatCount()
        val buffer = ByteBuffer.allocateDirect(floatCount * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        buffer.put(vertexData, 0, floatCount)
        buffer.position(0)
        GLES30.glBufferSubData(
            GLES30.GL_ARRAY_BUFFER, 0,
            floatCount * 4, buffer
        )

        GLES30.glDrawElements(
            GLES30.GL_TRIANGLES,
            spriteCount * 6,
            GLES30.GL_UNSIGNED_SHORT,
            0
        )

        GLES30.glBindVertexArray(0)
        texture.unbind()

        batch.reset()
    }

    fun dispose() {
        shader?.destroy()
        shader = null

        val buffers = intArrayOf(vboId, eboId)
        GLES30.glDeleteBuffers(2, buffers, 0)
        val vaos = intArrayOf(vaoId)
        GLES30.glDeleteVertexArrays(1, vaos, 0)
    }
}
