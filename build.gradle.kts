import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

group = "org.example"
version = "1.0-SNAPSHOT"

val localRepository: String by project
val kotlinVersion: String by project

repositories {
    maven(localRepository)
    mavenCentral()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += listOf("-Xcontext-receivers")
    }
}

val execute by tasks.creating(JavaExec::class) {
    dependsOn(tasks.compileKotlin)
    classpath = sourceSets.main.get().runtimeClasspath + sourceSets.main.get().compileClasspath
    main = "MainKt"
}

dependencies {
    kotlin("stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
}