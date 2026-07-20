pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "KaeEngine"

include(":engine-core")
include(":engine-math")
include(":engine-render")
include(":engine-physics")
include(":engine-input")
include(":engine-assets")
include(":engine-audio")
include(":engine-scene")
include(":engine-platform")
include(":sample-game")
