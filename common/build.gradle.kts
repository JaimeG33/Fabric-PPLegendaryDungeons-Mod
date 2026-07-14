plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
}

architectury {
    common("fabric", "neoforge")
}

loom {
    silentMojangMappingsLicense()

    mixin {
        defaultRefmapName.set("pp_legendarydungeons-common-refmap.json")
    }
}

dependencies {
    minecraft("net.minecraft:minecraft:${property("minecraft_version")}")
    mappings(loom.officialMojangMappings())

    modImplementation(
        "dev.architectury:architectury:${property("architectury_api_version")}"
    )

    modImplementation("com.cobblemon:mod:${property("cobblemon_version")}") {
        isTransitive = false
    }

    testImplementation(
        "org.junit.jupiter:junit-jupiter-api:${property("junit_version")}"
    )
    testRuntimeOnly(
        "org.junit.jupiter:junit-jupiter-engine:${property("junit_version")}"
    )
}
