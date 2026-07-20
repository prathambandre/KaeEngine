package com.kae.engine.scene

import kotlin.reflect.KClass

class World {
    var nextEntityId = 0
        private set
    val entities = mutableMapOf<Int, Int>()
    val destroyedEntities = mutableListOf<Int>()

    val componentStores = mutableMapOf<KClass<out Component>, MutableMap<Int, Component>>()
    private val systems = mutableListOf<System>()

    fun createEntity(): Entity {
        val id = if (destroyedEntities.isNotEmpty()) {
            destroyedEntities.removeAt(destroyedEntities.size - 1)
        } else {
            nextEntityId++
        }
        val version = entities[id]?.plus(1) ?: 0
        entities[id] = version
        return Entity(id, version)
    }

    fun destroyEntity(entity: Entity) {
        if (!isAlive(entity)) return
        for (store in componentStores.values) {
            store.remove(entity.id)
        }
        entities.remove(entity.id)
        destroyedEntities.add(entity.id)
    }

    fun isAlive(entity: Entity): Boolean {
        val version = entities[entity.id] ?: return false
        return version == entity.version
    }

    inline fun <reified T : Component> addComponent(entity: Entity, component: T) {
        if (!isAlive(entity)) return
        val store = componentStores.getOrPut(T::class) { mutableMapOf() }
        store[entity.id] = component
    }

    inline fun <reified T : Component> removeComponent(entity: Entity) {
        componentStores[T::class]?.remove(entity.id)
    }

    inline fun <reified T : Component> getComponent(entity: Entity): T? {
        if (!isAlive(entity)) return null
        return componentStores[T::class]?.get(entity.id) as? T
    }

    inline fun <reified T : Component> hasComponent(entity: Entity): Boolean {
        if (!isAlive(entity)) return false
        return componentStores[T::class]?.containsKey(entity.id) == true
    }

    inline fun <reified T : Component> getEntitiesWith(): List<Entity> {
        val store = componentStores[T::class] ?: return emptyList()
        return store.keys.mapNotNull { id ->
            val version = entities[id] ?: return@mapNotNull null
            Entity(id, version)
        }
    }

    inline fun <reified A : Component, reified B : Component> getEntitiesWithAB(): List<Entity> {
        val storeA = componentStores[A::class] ?: return emptyList()
        val storeB = componentStores[B::class] ?: return emptyList()
        val smaller = if (storeA.size <= storeB.size) storeA else storeB
        val larger = if (storeA.size <= storeB.size) storeB else storeA

        return smaller.keys.filter { larger.containsKey(it) }.mapNotNull { id ->
            val version = entities[id] ?: return@mapNotNull null
            Entity(id, version)
        }
    }

    inline fun <reified A : Component, reified B : Component, reified C : Component> getEntitiesWithABC(): List<Entity> {
        return getEntitiesWithAB<A, B>().filter { hasComponent<C>(it) }
    }

    inline fun <reified T : Component> forEach(action: (Entity, T) -> Unit) {
        val store = componentStores[T::class] ?: return
        for ((id, component) in store) {
            val version = entities[id] ?: continue
            action(Entity(id, version), component as T)
        }
    }

    inline fun <reified A : Component, reified B : Component> forEach(action: (Entity, A, B) -> Unit) {
        val entities = getEntitiesWithAB<A, B>()
        for (entity in entities) {
            val compA = getComponent<A>(entity) ?: continue
            val compB = getComponent<B>(entity) ?: continue
            action(entity, compA, compB)
        }
    }

    inline fun <reified A : Component, reified B : Component, reified C : Component> forEach(action: (Entity, A, B, C) -> Unit) {
        val entities = getEntitiesWithABC<A, B, C>()
        for (entity in entities) {
            val compA = getComponent<A>(entity) ?: continue
            val compB = getComponent<B>(entity) ?: continue
            val compC = getComponent<C>(entity) ?: continue
            action(entity, compA, compB, compC)
        }
    }

    fun addSystem(system: System) {
        if (system !in systems) {
            systems.add(system)
            systems.sortBy { it.priority }
            system.init(this)
        }
    }

    fun removeSystem(system: System) {
        if (systems.remove(system)) {
            system.destroy()
        }
    }

    fun getSystems(): List<System> = systems.toList()

    fun update(deltaTime: Float) {
        for (system in systems) {
            if (system.isEnabled) system.update(this, deltaTime)
        }
    }

    fun render(interpolation: Float) {
        for (system in systems) {
            if (system.isEnabled) system.render(this, interpolation)
        }
    }

    val entityCount: Int get() = entities.size

    fun getAllEntities(): List<Entity> {
        return entities.map { (id, version) -> Entity(id, version) }
    }

    fun clear() {
        for (system in systems) system.destroy()
        systems.clear()
        componentStores.clear()
        entities.clear()
        destroyedEntities.clear()
        nextEntityId = 0
    }
}
