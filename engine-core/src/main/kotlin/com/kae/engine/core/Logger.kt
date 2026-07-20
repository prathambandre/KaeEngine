package com.kae.engine.core

class Logger private constructor() {

    enum class Level {
        VERBOSE, DEBUG, INFO, WARN, ERROR
    }

    private var minLevel: Level = Level.DEBUG
    private val handlers: MutableList<(Level, String, String) -> Unit> = mutableListOf()

    fun setLevel(level: Level) {
        minLevel = level
    }

    fun getLevel(): Level = minLevel

    fun addHandler(handler: (Level, String, String) -> Unit) {
        handlers.add(handler)
    }

    fun clearHandlers() {
        handlers.clear()
    }

    fun log(level: Level, tag: String, message: String) {
        if (level.ordinal < minLevel.ordinal) return

        val formattedMessage = "[$level/$tag] $message"

        if (handlers.isEmpty()) {
            println(formattedMessage)
        } else {
            handlers.forEach { it(level, tag, message) }
        }
    }

    fun verbose(tag: String, message: String) {
        log(Level.VERBOSE, tag, message)
    }

    fun debug(tag: String, message: String) {
        log(Level.DEBUG, tag, message)
    }

    fun info(tag: String, message: String) {
        log(Level.INFO, tag, message)
    }

    fun warn(tag: String, message: String) {
        log(Level.WARN, tag, message)
    }

    fun error(tag: String, message: String, throwable: Throwable? = null) {
        val fullMessage = if (throwable != null) {
            "$message - ${throwable.message}"
        } else {
            message
        }
        log(Level.ERROR, tag, fullMessage)
    }

    companion object {
        val instance: Logger by lazy { Logger() }
    }
}
