buildscript {
    extra.apply {
        set("kotlinVersion", "1.9.22")
        set("compileSdk", 34)
        set("minSdk", 24)
        set("targetSdk", 34)
    }
}

plugins {
    id("com.android.application") version "8.2.2" apply false
    id("com.android.library") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("org.jetbrains.kotlin.jvm") version "1.9.22" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
