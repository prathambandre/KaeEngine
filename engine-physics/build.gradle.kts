plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":engine-math"))
    implementation(project(":engine-core"))
    implementation(project(":engine-scene"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.22")
}

tasks.test {
    useJUnitPlatform()
}
