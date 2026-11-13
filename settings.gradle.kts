pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.architectury.dev/")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.kikugie.dev/releases")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.8"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "forcedflight"

val minecraftVersions = listOf(
    "1.18.2", "1.19.2", "1.19.4", "1.20", "1.20.1", "1.20.2", "1.20.4", "1.20.6",
    "1.21", "1.21.1", "1.21.2", "1.21.3", "1.21.4", "1.21.5", "1.21.6", "1.21.7",
    "1.21.8", "1.21.9", "1.21.10", "1.21.11",
    "26.1", "26.1.1", "26.1.2", "26.2", "26.3",
)
// Forge never shipped 1.21.2. NeoForge starts at 1.20.2.
val noForge = setOf("1.21.2")
val noNeoForge = setOf("1.18.2", "1.19.2", "1.19.4", "1.20", "1.20.1")

fun loaderFor(projectName: String) = projectName.substringAfterLast('-')

for (mc in minecraftVersions) {
    for (loader in listOf("fabric", "forge", "neoforge")) {
        if (loader == "forge" && mc in noForge) continue
        if (loader == "neoforge" && mc in noNeoForge) continue
        val dir = file("versions/$mc-$loader")
        dir.mkdirs()
        file("versions/$mc-$loader/gradle.properties").writeText("loom.platform=$loader\n")
    }
}

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"
    create(rootProject) {
        val nodes = buildList {
            for (mc in minecraftVersions) {
                add("$mc-fabric" to mc)
                if (mc !in noForge) add("$mc-forge" to mc)
                if (mc !in noNeoForge) add("$mc-neoforge" to mc)
            }
        }
        versions(*nodes.toTypedArray())
        vcsVersion = "1.21.1-fabric"
    }
}
