plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "1.21.1-fabric" /* [SC] DO NOT EDIT */

tasks.register("buildAll") {
    group = "project"
    description = "Build every supported Minecraft version and loader"
    dependsOn(stonecutter.tasks.named("build"))
}
