plugins {
    id("com.gradleup.shadow")
    id("dev.architectury.loom")
    id("architectury-plugin")
}

architectury {
    platformSetupLoomIde()
    neoForge()
}

loom {
    enableTransitiveAccessWideners.set(true)
    silentMojangMappingsLicense()
}

val shadowCommon: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    minecraft("net.minecraft:minecraft:${property("minecraft_version")}")
    mappings(loom.officialMojangMappings())
    neoForge("net.neoforged:neoforge:${property("neoforge_version")}")

    modImplementation(
        "dev.architectury:architectury-neoforge:${property("architectury_api_version")}"
    )

    modImplementation(
        "com.cobblemon:neoforge:${property("cobblemon_version")}"
    )

    forgeRuntimeLibrary(
        "thedarkcolour:kotlinforforge-neoforge:${property("kotlin_for_forge_version")}"
    ) {
        exclude("net.neoforged.fancymodloader", "loader")
    }

    /*
     * Accessories' Architectury NeoForge development setup does not always
     * expose all of its jar-in-jar libraries to the launch classpath.
     */
    forgeRuntimeLibrary("io.wispforest:endec:0.1.8")
    forgeRuntimeLibrary("io.wispforest.endec:gson:0.1.5")
    forgeRuntimeLibrary("io.wispforest.endec:netty:0.1.4")
    forgeRuntimeLibrary("io.wispforest.endec:jankson:0.1.5")

    /*
     * The Endec Jankson adapter references the Jankson 1.x API directly.
     */
    forgeRuntimeLibrary("blue.endless:jankson:1.2.3")

    modRuntimeOnly(
        "io.wispforest:accessories-neoforge:${property("accessories_version")}"
    )
    modRuntimeOnly(
        "maven.modrinth:cobblemon-mega-showdown:${property("mega_showdown_version")}-neoforge"
    )

    implementation(project(":common", configuration = "namedElements"))
    "developmentNeoForge"(
        project(":common", configuration = "namedElements")
    ) {
        isTransitive = false
    }
    shadowCommon(
        project(":common", configuration = "transformProductionNeoForge")
    )

    testImplementation(
        "org.junit.jupiter:junit-jupiter-api:${property("junit_version")}"
    )
    testRuntimeOnly(
        "org.junit.jupiter:junit-jupiter-engine:${property("junit_version")}"
    )
}

tasks {
    processResources {
        inputs.property("version", project.version)

        filesMatching("META-INF/neoforge.mods.toml") {
            expand(project.properties)
        }
    }

    jar {
        archiveBaseName.set("cobblemon-eld")
        archiveClassifier.set("neoforge-dev-slim")
    }

    shadowJar {
        exclude("fabric.mod.json")
        archiveBaseName.set("cobblemon-eld")
        archiveClassifier.set("neoforge-dev-shadow")
        configurations = listOf(shadowCommon)
    }

    remapJar {
        dependsOn(shadowJar)
        inputFile.set(shadowJar.flatMap { it.archiveFile })
        archiveBaseName.set("cobblemon-eld")
        archiveVersion.set(project.version.toString())
        archiveClassifier.set("neoforge-mc1.21.1-cob1.7.3")
    }
}