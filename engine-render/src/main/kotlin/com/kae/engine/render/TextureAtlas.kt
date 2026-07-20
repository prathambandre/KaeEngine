package com.kae.engine.render

data class SpriteRegion(
    val u: Float,
    val v: Float,
    val u2: Float,
    val v2: Float,
    val width: Int,
    val height: Int
)

class TextureAtlas {

    val texture: Texture
        get() = atlasTexture

    private lateinit var atlasTexture: Texture
    private val regions = HashMap<String, SpriteRegion>()

    fun load(atlasTexture: Texture, regionDefinitions: Map<String, SpriteRegion>) {
        this.atlasTexture = atlasTexture
        regions.clear()
        regions.putAll(regionDefinitions)
    }

    fun getRegion(name: String): SpriteRegion? = regions[name]

    fun getRegionNames(): Set<String> = regions.keys.toSet()

    fun dispose() {
        atlasTexture.dispose()
        regions.clear()
    }
}
