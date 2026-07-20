package com.kae.engine.render

data class SpriteVertex(
    val x: Float,
    val y: Float,
    val u: Float,
    val v: Float,
    val r: Float,
    val g: Float,
    val b: Float,
    val a: Float
) {
    fun toFloatArray(): FloatArray = floatArrayOf(x, y, u, v, r, g, b, a)

    companion object {
        const val FLOATS_PER_VERTEX = 8
    }
}

class RenderBatch(maxSprites: Int = 10000) {

    companion object {
        const val MAX_SPRITES = 10000
        const val VERTICES_PER_SPRITE = 4
        const val INDICES_PER_SPRITE = 6
        const val MAX_VERTICES = MAX_SPRITES * VERTICES_PER_SPRITE
        const val MAX_INDICES = MAX_SPRITES * INDICES_PER_SPRITE
        const val FLOATS_PER_VERTEX = SpriteVertex.FLOATS_PER_VERTEX
        const val BYTES_PER_VERTEX = FLOATS_PER_VERTEX * 4
    }

    private val vertexData = FloatArray(MAX_VERTICES * FLOATS_PER_VERTEX)
    private var vertexCount: Int = 0
    private var spriteCount: Int = 0

    fun getVertices(): FloatArray = vertexData

    fun getVertexCount(): Int = vertexCount

    fun getSpriteCount(): Int = spriteCount

    fun getFloatCount(): Int = vertexCount * FLOATS_PER_VERTEX

    fun addSprite(
        x: Float, y: Float, width: Float, height: Float,
        u: Float, v: Float, u2: Float, v2: Float,
        r: Float, g: Float, b: Float, a: Float
    ) {
        if (spriteCount >= MAX_SPRITES) return

        val i = vertexCount * FLOATS_PER_VERTEX

        vertexData[i + 0] = x
        vertexData[i + 1] = y
        vertexData[i + 2] = u
        vertexData[i + 3] = v
        vertexData[i + 4] = r
        vertexData[i + 5] = g
        vertexData[i + 6] = b
        vertexData[i + 7] = a

        vertexData[i + 8] = x + width
        vertexData[i + 9] = y
        vertexData[i + 10] = u2
        vertexData[i + 11] = v
        vertexData[i + 12] = r
        vertexData[i + 13] = g
        vertexData[i + 14] = b
        vertexData[i + 15] = a

        vertexData[i + 16] = x + width
        vertexData[i + 17] = y + height
        vertexData[i + 18] = u2
        vertexData[i + 19] = v2
        vertexData[i + 20] = r
        vertexData[i + 21] = g
        vertexData[i + 22] = b
        vertexData[i + 23] = a

        vertexData[i + 24] = x
        vertexData[i + 25] = y + height
        vertexData[i + 26] = u
        vertexData[i + 27] = v2
        vertexData[i + 28] = r
        vertexData[i + 29] = g
        vertexData[i + 30] = b
        vertexData[i + 31] = a

        vertexCount += VERTICES_PER_SPRITE
        spriteCount++
    }

    fun addRotatedSprite(
        x: Float, y: Float, width: Float, height: Float,
        u: Float, v: Float, u2: Float, v2: Float,
        r: Float, g: Float, b: Float, a: Float,
        rotation: Float, originX: Float, originY: Float
    ) {
        if (spriteCount >= MAX_SPRITES) return

        val radians = Math.toRadians(rotation.toDouble()).toFloat()
        val cosA = cos(radians)
        val sinA = sin(radians)

        val cx = x + width * originX
        val cy = y + height * originY

        val corners = arrayOf(
            floatArrayOf(x, y),
            floatArrayOf(x + width, y),
            floatArrayOf(x + width, y + height),
            floatArrayOf(x, y + height)
        )
        val uvs = arrayOf(
            floatArrayOf(u, v),
            floatArrayOf(u2, v),
            floatArrayOf(u2, v2),
            floatArrayOf(u, v2)
        )

        for (i in 0..3) {
            val px = corners[i][0] - cx
            val py = corners[i][1] - cy
            val rx = px * cosA - py * sinA + cx
            val ry = px * sinA + py * cosA + cy

            val vi = (vertexCount + i) * FLOATS_PER_VERTEX
            vertexData[vi + 0] = rx
            vertexData[vi + 1] = ry
            vertexData[vi + 2] = uvs[i][0]
            vertexData[vi + 3] = uvs[i][1]
            vertexData[vi + 4] = r
            vertexData[vi + 5] = g
            vertexData[vi + 6] = b
            vertexData[vi + 7] = a
        }

        vertexCount += VERTICES_PER_SPRITE
        spriteCount++
    }

    fun reset() {
        vertexCount = 0
        spriteCount = 0
    }

    fun isFull(): Boolean = spriteCount >= MAX_SPRITES

    private fun cos(rad: Float): Float = kotlin.math.cos(rad)
    private fun sin(rad: Float): Float = kotlin.math.sin(rad)
}
