plugins {
    id("application")
    kotlin("jvm") version "1.5.30-M1"
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "com.uramnoil"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClass.set("com/uramnoil/nukkitconsolemanager.MainKt")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
}