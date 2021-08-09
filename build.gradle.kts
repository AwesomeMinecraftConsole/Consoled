plugins {
    kotlin("jvm") version "1.5.30-M1"
}

group = "com.uramnoil"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
}

projectDir.absolutePath