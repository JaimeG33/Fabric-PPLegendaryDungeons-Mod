import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java")
    id("java-library")
    kotlin("jvm") version "2.2.20"
    id("com.gradleup.shadow") version "9.3.1" apply false
    id("dev.architectury.loom") version "1.11-SNAPSHOT" apply false
    id("architectury-plugin") version "3.4-SNAPSHOT" apply false
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    group = property("maven_group")!!
    version = property("mod_version")!!

    repositories {
        mavenCentral()

        // Cobblemon
        maven("https://artefacts.cobblemon.com/releases/")

        // Mega Showdown
        maven("https://api.modrinth.com/maven")

        // Accessories and owo-lib
        maven("https://maven.wispforest.io/releases")
        maven("https://maven.su5ed.dev/releases")
        maven("https://maven.shedaniel.me/")

        // Fabric libraries used by transitive cross-loader dependencies
        maven("https://maven.fabricmc.net/")

        // Forgified Fabric API dependencies used by NeoForge libraries
        maven("https://maven.sinytra.org")

        // Architectury and NeoForge
        maven("https://maven.architectury.dev/")
        maven("https://maven.neoforged.net/releases/")

        // Kotlin for Forge
        maven("https://thedarkcolour.github.io/KotlinForForge/")
    }

    tasks {
        test {
            useJUnitPlatform()
        }

        java {
            withSourcesJar()
            sourceCompatibility = JavaVersion.VERSION_21
            targetCompatibility = JavaVersion.VERSION_21
        }

        compileJava {
            options.release = 21
        }

        compileKotlin {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_21)
            }
        }
    }
}

// The root project coordinates the modules and should not publish an empty JAR.
tasks.named<org.gradle.jvm.tasks.Jar>("jar") {
    enabled = false
}

tasks.named<org.gradle.jvm.tasks.Jar>("sourcesJar") {
    enabled = false
}
