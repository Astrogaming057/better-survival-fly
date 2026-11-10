architectury {
    common("forge", "fabric", "neoforge")
    platformSetupLoomIde()
}

loom.accessWidenerPath.set(file("src/main/resources/forcedflight.accesswidener"))

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${providers.gradleProperty("fabric_loader_version").get()}")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
