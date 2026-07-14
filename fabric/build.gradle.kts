plugins {
    id("com.gradleup.shadow")
    id("dev.architectury.loom")
    id("architectury-plugin")
}

architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
    enableTransitiveAccessWideners.set(true)
    silentMojangMappingsLicense()

    runs {
        named("client") {
            vmArg("-Xms2G")
            vmArg("-Xmx6G")
        }
    }
}

val shadowCommon: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    minecraft("net.minecraft:minecraft:${property("minecraft_version")}")
    mappings(loom.officialMojangMappings())

    modImplementation(
        "net.fabricmc:fabric-loader:${property("fabric_loader_version")}"
    )
    modImplementation(
        "net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}"
    )
    modImplementation(
        "net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}"
    )
    modImplementation(
        "dev.architectury:architectury-fabric:${property("architectury_api_version")}"
    )

    modImplementation("com.cobblemon:fabric:${property("cobblemon_version")}")

    modRuntimeOnly(
        "io.wispforest:accessories-fabric:${property("accessories_version")}"
    )
    modRuntimeOnly(
        "maven.modrinth:cobblemon-mega-showdown:${property("mega_showdown_version")}-fabric"
    )

    implementation(project(":common", configuration = "namedElements"))
    "developmentFabric"(
        project(":common", configuration = "namedElements")
    )
    shadowCommon(
        project(":common", configuration = "transformProductionFabric")
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

        filesMatching("fabric.mod.json") {
            expand(project.properties)
        }
    }

    jar {
        archiveBaseName.set("cobblemon-eld")
        archiveClassifier.set("fabric-dev-slim")
    }

    shadowJar {
        archiveBaseName.set("cobblemon-eld")
        archiveClassifier.set("fabric-dev-shadow")
        configurations = listOf(shadowCommon)
    }

    remapJar {
        dependsOn(shadowJar)
        inputFile.set(shadowJar.flatMap { it.archiveFile })
        archiveBaseName.set("cobblemon-eld")
        archiveVersion.set(project.version.toString())
        archiveClassifier.set("fabric-mc1.21.1-cob1.7.3")
    }
}
