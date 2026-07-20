plugins {
    id("com.android.library") version "8.2.2"
    id("org.jetbrains.kotlin.android") version "1.9.22"
}

android {
    namespace = "com.kae.engine.platform"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    api(project(":engine-core"))
    api(project(":engine-render"))
    api(project(":engine-input"))
    api(project(":engine-assets"))
    api(project(":engine-audio"))
    api(project(":engine-math"))
    api(project(":engine-scene"))
    api(project(":engine-physics"))
    implementation("androidx.appcompat:appcompat:1.6.1")
}
