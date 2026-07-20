package com.kae.engine.assets

import org.json.JSONObject
import org.json.JSONArray

class SceneLoader(private val assetManager: AssetManager) {

    data class Scene(
        val name: String,
        val entities: List<EntityData>
    )

    data class EntityData(
        val name: String,
        val components: Map<String, Map<String, Any>>
    )

    fun loadScene(jsonPath: String, world: com.kae.engine.scene.World): Scene? {
        val jsonString = assetManager.loadSync(jsonPath, AssetType.JSON) ?: return null
        return parseScene(jsonString, world)
    }

    private fun parseScene(jsonString: String, world: com.kae.engine.scene.World): Scene? {
        return try {
            val root = JSONObject(jsonString)
            val name = root.optString("name", "untitled")
            val entitiesJson = root.optJSONArray("entities") ?: JSONArray()
            val entities = mutableListOf<EntityData>()

            for (i in 0 until entitiesJson.length()) {
                val entityJson = entitiesJson.getJSONObject(i)
                val entityData = parseEntity(entityJson)
                entities.add(entityData)

                val entity = mutableMapOf<String, Any>()
                entity["name"] = entityData.name
                entity["components"] = entityData.components
                world.createEntity()
            }

            Scene(name, entities)
        } catch (e: Exception) {
            null
        }
    }

    fun parseEntity(json: JSONObject): EntityData {
        val name = json.optString("name", "unnamed")
        val componentsJson = json.optJSONObject("components") ?: JSONObject()
        val components = mutableMapOf<String, Map<String, Any>>()

        for (key in componentsJson.keys()) {
            val componentJson = componentsJson.getJSONObject(key)
            components[key] = parseComponent(componentJson)
        }

        return EntityData(name, components)
    }

    private fun parseComponent(json: JSONObject): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        for (key in json.keys()) {
            map[key] = json.get(key)
        }
        return map
    }

    private fun parseComponentValue(value: Any): Any {
        return when (value) {
            is JSONObject -> {
                val map = mutableMapOf<String, Any>()
                for (key in value.keys()) {
                    map[key] = parseComponentValue(value.get(key))
                }
                map
            }
            is JSONArray -> {
                val list = mutableListOf<Any>()
                for (i in 0 until value.length()) {
                    list.add(parseComponentValue(value.get(i)))
                }
                list
            }
            else -> value
        }
    }
}
