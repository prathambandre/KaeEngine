package com.kae.engine.core

interface RendererInterface {
    fun initialize(width: Int, height: Int)
    fun beginFrame()
    fun endFrame()
    fun setViewport(width: Int, height: Int)
    fun clear()
    fun destroy()
}
