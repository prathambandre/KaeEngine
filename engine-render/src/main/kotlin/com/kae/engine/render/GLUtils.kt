package com.kae.engine.render

import android.opengl.GLES30

object GLUtils {

    fun checkGLError(label: String) {
        val error = GLES30.glGetError()
        if (error != GLES30.GL_NO_ERROR) {
            throw RuntimeException(
                "GL error after $label: 0x${Integer.toHexString(error)}"
            )
        }
    }

    fun loadShader(type: Int, source: String): Int {
        val shader = GLES30.glCreateShader(type)
        if (shader == 0) {
            throw RuntimeException("Failed to create shader of type $type")
        }
        GLES30.glShaderSource(shader, source)
        GLES30.glCompileShader(shader)

        val compiled = IntArray(1)
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            val log = GLES30.glGetShaderInfoLog(shader)
            GLES30.glDeleteShader(shader)
            throw RuntimeException("Shader compilation failed:\n$log")
        }
        return shader
    }

    fun createProgram(vertexSource: String, fragmentSource: String): Int {
        val vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexSource)
        val fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentSource)

        val program = GLES30.glCreateProgram()
        if (program == 0) {
            throw RuntimeException("Failed to create program")
        }
        GLES30.glAttachShader(program, vertexShader)
        GLES30.glAttachShader(program, fragmentShader)
        GLES30.glLinkProgram(program)

        val linked = IntArray(1)
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linked, 0)
        if (linked[0] == 0) {
            val log = GLES30.glGetProgramInfoLog(program)
            GLES30.glDeleteProgram(program)
            throw RuntimeException("Program linking failed:\n$log")
        }

        GLES30.glDeleteShader(vertexShader)
        GLES30.glDeleteShader(fragmentShader)

        return program
    }

    fun deleteShader(shaderId: Int) {
        if (shaderId != 0) {
            GLES30.glDeleteShader(shaderId)
        }
    }

    fun deleteProgram(programId: Int) {
        if (programId != 0) {
            GLES30.glDeleteProgram(programId)
        }
    }
}
