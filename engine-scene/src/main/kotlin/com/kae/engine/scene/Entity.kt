package com.kae.engine.scene

data class Entity(val id: Int, val version: Int = 0) {
    companion object {
        val INVALID = Entity(-1, -1)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Entity) return false
        return id == other.id && version == other.version
    }

    override fun hashCode(): Int = id * 31 + version
    override fun toString(): String = "Entity(id=$id, v=$version)"
}
