import org.gradle.jvm.tasks.Jar
import java.io.File

plugins {
    id("dev.kikugie.stonecutter")
    id("dev.architectury.loom") version "1.14.473" apply false
    id("dev.architectury.loom-no-remap") version "1.14.473" apply false
}

data class Target(
    val fabricApi: String,
    val forge: String? = null,
    val neoforge: String? = null,
    val java: Int,
)

val targets = mapOf(
    "1.18.2" to Target("0.77.0+1.18.2", forge = "40.3.12", java = 17),
    "1.19.2" to Target("0.77.0+1.19.2", forge = "43.5.2", java = 17),
    "1.19.4" to Target("0.87.2+1.19.4", forge = "45.4.5", java = 17),
    "1.20" to Target("0.83.0+1.20", forge = "46.0.14", java = 17),
    "1.20.1" to Target("0.92.12+1.20.1", forge = "47.4.29", java = 17),
    "1.20.2" to Target("0.91.6+1.20.2", forge = "48.1.0", neoforge = "20.2.93", java = 17),
    "1.20.4" to Target("0.97.3+1.20.4", forge = "49.2.13", neoforge = "20.4.253", java = 17),
    "1.20.6" to Target("0.100.8+1.20.6", forge = "50.2.15", neoforge = "20.6.144", java = 21),
    "1.21" to Target("0.102.0+1.21", forge = "51.0.33", neoforge = "21.0.167", java = 21),
    "1.21.1" to Target("0.116.18+1.21.1", forge = "52.1.21", neoforge = "21.1.266", java = 21),
    "1.21.2" to Target("0.106.1+1.21.2", neoforge = "21.2.1-beta", java = 21),
    "1.21.3" to Target("0.114.1+1.21.3", forge = "53.1.16", neoforge = "21.3.100", java = 21),
    "1.21.4" to Target("0.119.4+1.21.4", forge = "54.1.22", neoforge = "21.4.161", java = 21),
    "1.21.5" to Target("0.128.2+1.21.5", forge = "55.1.18", neoforge = "21.5.101", java = 21),
    "1.21.6" to Target("0.128.2+1.21.6", forge = "56.0.9", neoforge = "21.6.20-beta", java = 21),
    "1.21.7" to Target("0.129.0+1.21.7", forge = "57.0.3", neoforge = "21.7.25-beta", java = 21),
    "1.21.8" to Target("0.136.1+1.21.8", forge = "58.1.26", neoforge = "21.8.57", java = 21),
    "1.21.9" to Target("0.134.1+1.21.9", forge = "59.0.5", neoforge = "21.9.16-beta", java = 21),
    "1.21.10" to Target("0.138.4+1.21.10", forge = "60.1.18", neoforge = "21.10.64", java = 21),
    "1.21.11" to Target("0.141.6+1.21.11", forge = "61.2.5", neoforge = "21.11.48", java = 21),
    "26.1" to Target("0.145.1+26.1", forge = "62.0.9", neoforge = "26.1.0.19-beta", java = 25),
    "26.1.1" to Target("0.145.4+26.1.1", forge = "63.0.2", neoforge = "26.1.1.15-beta", java = 25),
    "26.1.2" to Target("0.155.3+26.1.2", forge = "64.1.6", neoforge = "26.1.2.126", java = 25),
    "26.2" to Target("0.161.0+26.2", forge = "65.1.6", neoforge = "26.2.0.93", java = 25),
    "26.3" to Target("0.166.2+26.3", forge = "66.1.8", neoforge = "26.3.0.103", java = 25),
)

val current = stonecutter.current.project
val mcVersion = stonecutter.current.version
val loaderName = current.substringAfterLast('-')
val isFabric = loaderName == "fabric"
val isNeoForge = loaderName == "neoforge"
val isForge = loaderName == "forge"
val isUnobfuscated = stonecutter.eval(mcVersion, ">=26.1")
val target = targets.getValue(mcVersion)
val javaInt = target.java
val loader = when {
    isFabric -> "fabric"
    isNeoForge -> "neoforge"
    else -> "forge"
}

layout.buildDirectory.set(rootProject.layout.projectDirectory.dir("versions/$javaInt/$mcVersion/$loader/build"))

stonecutter {
    constants["fabric"] = isFabric
    constants["neoforge"] = isNeoForge
    constants["forge"] = isForge
}

// Unobfuscated Forge has an empty MCP pipeline, and Architectury Loom NPEs while patching it.
extra["loom.platform"] = if (isForge && isUnobfuscated) "fabric" else loader

if (isUnobfuscated) {
    apply(plugin = "dev.architectury.loom-no-remap")
} else {
    apply(plugin = "dev.architectury.loom")
}

version = "${property("mod_version")}+$mcVersion"
group = property("maven_group") as String
base {
    archivesName.set("forcedflight-$loader")
}

fun upperBound(version: String): String {
    val parts = version.split(".")
    if (parts.size <= 2) {
        return "${parts[0]}.${parts[1]}.1"
    }
    val next = parts.map { it.toInt() }.toMutableList()
    next[next.lastIndex] = next.last() + 1
    return next.joinToString(".")
}

fun loaderRange(loaderVersion: String, keep: Int): String {
    val parts = loaderVersion.substringBefore("-").split(".")
    val base = parts.take(keep)
    val next = base.toMutableList()
    next[next.lastIndex] = (next.last().toInt() + 1).toString()
    return "[${base.joinToString(".")},${next.joinToString(".")})"
}

val mcUpper = upperBound(mcVersion)
val mcRange = "[$mcVersion,$mcUpper)"
val fabricMc = ">=$mcVersion <$mcUpper"
val loaderRange = when {
    isForge -> loaderRange(target.forge!!, 1)
    isNeoForge -> {
        val pieces = target.neoforge!!.substringBefore("-").split(".")
        loaderRange(target.neoforge, if (pieces.size >= 4) 3 else 2)
    }
    else -> "[1,)"
}
val fmlRange = if (isNeoForge && stonecutter.eval(mcVersion, ">=1.21")) "[4,)" else "[1,)"

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://maven.minecraftforge.net/")
    maven("https://maven.neoforged.net/releases/")
}

val loom = extensions.getByType<net.fabricmc.loom.api.LoomGradleExtensionAPI>()
loom.silentMojangMappingsLicense()
if (!isUnobfuscated) {
    loom.accessWidenerPath.set(rootProject.file("src/main/resources/forcedflight.accesswidener"))
}
if (isForge && !isUnobfuscated) {
    loom.mixin.useLegacyMixinAp.set(true)
    loom.forge.apply {
        convertAccessWideners.set(true)
        mixinConfig("forcedflight.mixins.json")
    }
}

dependencies {
    "minecraft"("com.mojang:minecraft:$mcVersion")
    if (!isUnobfuscated) {
        "mappings"(loom.officialMojangMappings())
    }
    val modImpl = if (isUnobfuscated) "implementation" else "modImplementation"
    if (isFabric) {
        modImpl("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
        modImpl("net.fabricmc.fabric-api:fabric-api:${target.fabricApi}")
    } else if (isNeoForge) {
        "neoForge"("net.neoforged:neoforge:${target.neoforge}")
    } else if (isUnobfuscated) {
        // Forge's own userdev pipeline NPEs on these versions (empty MCP config).
        // These coordinates match that userdev config.json. Resolving the userdev jar
        // here would use Maven before Loom can register its library repositories.
        val forgeVer = "$mcVersion-${target.forge}"
        val since1212 = stonecutter.eval(mcVersion, ">=26.1.2")
        val since26_2 = stonecutter.eval(mcVersion, ">=26.2")
        val since26_3 = stonecutter.eval(mcVersion, ">=26.3")
        val asm = if (since1212) "9.10.1" else "9.9.1"
        listOf(
            "net.minecraftforge:forge:$forgeVer:universal",
            "net.minecraftforge:eventbus:${if (since26_3) "7.0.6" else if (since1212) "7.0.5" else "7.0.1"}",
            "org.jspecify:jspecify:${if (since26_3) "1.0.1" else "1.0.0"}",
            "net.minecraftforge:forgespi:8.0.0",
            "net.minecraftforge:coremods-api:${if (since1212) "5.3.1" else "5.3.0"}",
            "net.minecraftforge:modlauncher:${if (since1212) "10.2.6" else "10.2.4"}",
            "org.spongepowered:mixin:0.8.7",
            "io.github.llamalad7:mixinextras-forge:${if (since1212) "0.5.4" else "0.5.3"}",
            "net.minecraftforge:fmlcore:$forgeVer",
            "net.minecraftforge:fmlloader:$forgeVer",
            "net.minecraftforge:javafmllanguage:$forgeVer",
            "net.minecraftforge:lowcodelanguage:$forgeVer",
            "net.minecraftforge:mclanguage:$forgeVer",
            "com.google.guava:guava:${if (since26_2) "33.6.0-jre" else "33.5.0-jre"}",
            "org.ow2.asm:asm:$asm",
            "org.ow2.asm:asm-tree:$asm",
            "org.ow2.asm:asm-commons:$asm",
        ).forEach { modImpl(it) }
    } else {
        "forge"("net.minecraftforge:forge:$mcVersion-${target.forge}")
    }
    "testImplementation"("org.junit.jupiter:junit-jupiter:5.10.3")
    "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
}

val packExtra = when {
    stonecutter.eval(mcVersion, ">=1.21.9") ->
        """,
    "supported_formats": {
      "min_inclusive": 1,
      "max_inclusive": 1000
    },
    "min_format": 1,
    "max_format": 1000"""
    stonecutter.eval(mcVersion, ">=1.20.2") ->
        """,
    "supported_formats": [1, 1000]"""
    else -> ""
}
val expandVars = mapOf(
    "version" to version.toString(),
    "mc_dep" to fabricMc,
    "mc_range" to mcRange,
    "loader_range" to loaderRange,
    "fml_range" to fmlRange,
    "java_version" to javaInt.toString(),
    "java_compat" to javaInt.toString(),
    "access_widener_line" to if (isUnobfuscated) "" else "\"accessWidener\": \"forcedflight.accesswidener\",",
    "pack_extra" to packExtra,
    "destroy_speed_mixin" to if (isFabric) "    \"PlayerDestroySpeedMixin\",\n" else "",
)

tasks.named<ProcessResources>("processResources") {
    if (!isFabric) {
        exclude("fabric.mod.json")
    }
    if (!isForge) {
        exclude("META-INF/mods.toml")
    }
    if (!isNeoForge || stonecutter.eval(mcVersion, "<1.20.5")) {
        exclude("META-INF/neoforge.mods.toml")
    }
    inputs.properties(expandVars)
    filesMatching(listOf("fabric.mod.json", "forcedflight.mixins.json", "pack.mcmeta", "META-INF/mods.toml", "META-INF/neoforge.mods.toml")) {
        expand(expandVars)
    }
}

if (isNeoForge && stonecutter.eval(mcVersion, "<1.20.5")) {
    tasks.named<ProcessResources>("processResources") {
        from(rootProject.file("src/main/resources/META-INF/neoforge.mods.toml")) {
            into("META-INF")
            rename { "mods.toml" }
            expand(expandVars)
        }
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(javaInt))
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    if (!isFabric) {
        exclude("com/forcedflight/fabric/**")
        exclude("com/forcedflight/mixin/PlayerDestroySpeedMixin.java")
    }
    if (!isNeoForge) {
        exclude("com/forcedflight/neoforge/**")
    }
    if (!isForge) {
        exclude("com/forcedflight/forge/**")
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    enabled = current == "1.21.1-fabric"
}

tasks.named<JavaCompile>("compileTestJava") {
    enabled = current == "1.21.1-fabric"
}

tasks.named<Jar>("jar") {
    from(rootProject.file("LICENSE")) {
        rename { "${it}_${base.archivesName.get()}" }
    }
}

if (!isUnobfuscated) {
    // Forge 1.20.6+ loads Mojmap at runtime. Remapping named -> named makes Loom
    // think namespaces match and fail. Ship the named jar instead of remapping to SRG.
    val forgeKeepsMojmap = isForge && stonecutter.eval(mcVersion, ">=1.20.6")
    tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
        archiveClassifier.set("")
        if (forgeKeepsMojmap) {
            enabled = false
        }
    }
    if (forgeKeepsMojmap) {
        tasks.named<Jar>("jar") {
            archiveClassifier.set("")
            doLast {
                val built = archiveFile.get().asFile
                val libs = layout.buildDirectory.dir("libs").get().asFile
                libs.mkdirs()
                built.copyTo(File(libs, built.name), overwrite = true)
            }
        }
        tasks.named<net.fabricmc.loom.task.RemapSourcesJarTask>("remapSourcesJar") {
            enabled = false
        }
        tasks.named("assemble") {
            dependsOn("jar")
        }
    }
}
