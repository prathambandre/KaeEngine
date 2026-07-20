package com.kae.engine.render

import android.content.Context
import android.opengl.GLES30
import com.kae.engine.math.Mat4
import com.kae.engine.math.Vec3
import com.kae.engine.math.Vec4
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Shader {

    var programId: Int = 0
        private set

    private val uniformCache = HashMap<String, Int>()

    constructor(context: Context, vertexResId: Int, fragmentResId: Int) {
        val vertexSource = loadRawText(context, vertexResId)
        val fragmentSource = loadRawText(context, fragmentResId)
        programId = GLUtils.createProgram(vertexSource, fragmentSource)
    }

    constructor(vertexSource: String, fragmentSource: String) {
        programId = GLUtils.createProgram(vertexSource, fragmentSource)
    }

    fun use() {
        GLES30.glUseProgram(programId)
    }

    fun bind() = use()

    fun setMat4(name: String, matrix: Mat4) {
        val location = getUniformLocation(name)
        val buffer = ByteBuffer.allocateDirect(16 * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        buffer.put(matrix.toFloatArray())
        buffer.position(0)
        GLES30.glUniformMatrix4fv(location, 1, false, buffer)
    }

    fun setVec3(name: String, vec: Vec3) {
        val location = getUniformLocation(name)
        GLES30.glUniform3f(location, vec.x, vec.y, vec.z)
    }

    fun setVec4(name: String, vec: Vec4) {
        val location = getUniformLocation(name)
        GLES30.glUniform4f(location, vec.x, vec.y, vec.z, vec.w)
    }

    fun setFloat(name: String, value: Float) {
        val location = getUniformLocation(name)
        GLES30.glUniform1f(location, value)
    }

    fun setInt(name: String, value: Int) {
        val location = getUniformLocation(name)
        GLES30.glUniform1i(location, value)
    }

    fun setBool(name: String, value: Boolean) {
        setInt(name, if (value) 1 else 0)
    }

    fun destroy() {
        if (programId != 0) {
            GLUtils.deleteProgram(programId)
            programId = 0
        }
        uniformCache.clear()
    }

    private fun getUniformLocation(name: String): Int {
        return uniformCache.getOrPut(name) {
            GLES30.glGetUniformLocation(programId, name)
        }
    }

    private fun loadRawText(context: Context, resId: Int): String {
        return context.resources.openRawResource(resId).bufferedReader().use { it.readText() }
    }
}
