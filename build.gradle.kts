
plugins {
    id("java")
    id("dev.architectury.loom") version ("1.11-SNAPSHOT")
    id("architectury-plugin") version ("3.4-SNAPSHOT")
    kotlin("jvm") version ("2.2.20")
}

group = "porker.pp_legendarydungeons"
version = "1.0.2"
base {
    archivesName.set(
        "cobblemon-eld-${project.version}-fabricmc1.21.1-cob1.7.3"
    )
}

architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
    silentMojangMappingsLicense()

    runs {
        named("client") {
            vmArg("-Xms2G")
            vmArg("-Xmx6G")
        }
    }

    mixin {
        defaultRefmapName.set("mixins.${project.name}.refmap.json")
    }
}

repositories {
    mavenCentral()

    // Cobblemon
    maven("https://artefacts.cobblemon.com/releases/")

    // Mega Showdown from Modrinth Maven
    maven("https://api.modrinth.com/maven")

    // Accessories, required by Mega Showdown
    maven("https://maven.wispforest.io/releases")

    // Architectury API, required by Mega Showdown
    maven("https://maven.architectury.dev/")
}

dependencies {
    minecraft("net.minecraft:minecraft:1.21.1")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:0.17.2")

    /*
     * Full Fabric API is now a compile dependency because dungeon rules use:
     * - interaction callbacks
     * - networking payloads
     * - lifecycle/tick events
     */
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.116.6+1.21.1")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.13.6+kotlin.2.2.20")


    modCompileOnly("com.cobblemon:mod:1.7.3+1.21.1") {
        isTransitive = false
    }
    modImplementation("com.cobblemon:fabric:1.7.3+1.21.1")
    /*
 * Mega Showdown is currently used through item IDs, structure IDs,
 * tags, loot tables, and other data-driven references.
 *
 * Runtime-only is sufficient unless Java classes from Mega Showdown
 * are imported into this project later.
 */
    modRuntimeOnly("dev.architectury:architectury-fabric:13.0.8")
    modRuntimeOnly("io.wispforest:accessories-fabric:1.1.0-beta.52+1.21.1")
    modRuntimeOnly(
        "maven.modrinth:cobblemon-mega-showdown:1.6.9+1.7.3+1.21.1-fabric"
    )

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
}

tasks {
    test {
        useJUnitPlatform()
    }

    processResources {
        inputs.property("version", project.version)

        filesMatching("fabric.mod.json") {
            expand(project.properties)
        }
    }

    java {
        withSourcesJar()
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    compileJava {
        options.release = 21
    }
}
