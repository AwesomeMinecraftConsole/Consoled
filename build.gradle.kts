import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

plugins {
    id("application")
    kotlin("jvm") version "1.5.30-M1"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("com.google.protobuf") version "0.8.15"
}

group = "com.uramnoil"
version = "0.1.0"

repositories {
    google()
    mavenCentral()
}

application {
    mainClass.set("com/uramnoil/nukkitconsolemanager.MainKt")
}

java {
    sourceSets.main {
        java.srcDirs("build/generated/source/proto/main/grpc")
        java.srcDirs("build/generated/source/proto/main/java")
    }
}

kotlin {
    sourceSets.main {
        kotlin.srcDirs("build/generated/source/proto/main/grpckt")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

val protobufVersion = "3.17.3"
val grpcVersion = "1.39.0"
val grpcKotlinVersion = "1.1.0"

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
    implementation("com.google.protobuf:protobuf-java-util:$protobufVersion")
    implementation("io.grpc:grpc-netty-shaded:$grpcVersion")
    implementation("io.grpc:grpc-protobuf:$grpcVersion")
    implementation("io.grpc:grpc-kotlin-stub:$grpcKotlinVersion")

    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.12")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion${if (osdetector.os == "osx") ":osx-x86_64" else ""}"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:$grpcKotlinVersion:jdk7@jar"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc")
                id("grpckt")
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}