package com.kae.engine.platform

import android.content.Context
import android.opengl.EGLConfig
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import com.kae.engine.core.Engine
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.opengles.GL10

class EngineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GLSurfaceView(context, attrs) {

    private var engineGLRenderer: EngineGLRenderer? = null
    var engine: Engine? = null
        private set

    init {
        setEGLContextClientVersion(3)
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
    }

    fun initialize(engine: Engine) {
        this.engine = engine
        engineGLRenderer = EngineGLRenderer(engine)
        setRenderer(engineGLRenderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return super.onTouchEvent(event)
    }
}

private class EngineGLRenderer(private val engine: Engine) : GLSurfaceView.Renderer {

    private var startTimeNanos: Long = 0L

    override fun onSurfaceCreated(gl: GL10?, config: javax.microedition.khronos.egl.EGLConfig?) {
        startTimeNanos = System.nanoTime()
        GLES30.glClearColor(0f, 0f, 0f, 1f)
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
        GLES30.glDisable(GLES30.GL_DEPTH_TEST)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        engine.renderer?.setViewport(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        val currentTimeNanos = System.nanoTime()
        val deltaTime = (currentTimeNanos - startTimeNanos) / 1_000_000_000f
        startTimeNanos = currentTimeNanos
        engine.mainLoop(deltaTime)
    }
}
