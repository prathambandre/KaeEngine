package com.kae.engine.render

import android.opengl.GLES30
import com.kae.engine.core.RendererInterface
import com.kae.engine.math.Vec4

class Renderer : RendererInterface {

    var clearColor: Vec4 = Vec4(0f, 0f, 0f, 1f)

    private var defaultShader: Shader? = null
    private var spriteBatch: SpriteBatch? = null
    private var initialized: Boolean = false

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

    override fun initialize(width: Int, height: Int) {
        GLES30.glClearColor(clearColor.x, clearColor.y, clearColor.z, clearColor.w)
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
        GLES30.glDisable(GLES30.GL_DEPTH_TEST)
        GLES30.glViewport(0, 0, width, height)

        defaultShader = Shader(defaultVertexShader, defaultFragmentShader)
        spriteBatch = SpriteBatch()

        initialized = true
    }

    override fun beginFrame() {
        GLES30.glClearColor(clearColor.x, clearColor.y, clearColor.z, clearColor.w)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
    }

    override fun endFrame() {
        // Buffer swap is handled by the platform layer (GLSurfaceView/eglSwapBuffers)
    }

    override fun setViewport(width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
    }

    override fun clear() {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
    }

    override fun destroy() {
        defaultShader?.destroy()
        defaultShader = null
        spriteBatch?.dispose()
        spriteBatch = null
        initialized = false
    }

    fun getSpriteBatch(): SpriteBatch {
        return spriteBatch ?: throw IllegalStateException("Renderer not initialized")
    }

    fun getDefaultShader(): Shader {
        return defaultShader ?: throw IllegalStateException("Renderer not initialized")
    }

    fun isInitialized(): Boolean = initialized
}
