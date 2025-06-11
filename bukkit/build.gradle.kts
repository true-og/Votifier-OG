import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    id("com.gradleup.shadow")
}

applyPlatformAndCoreConfiguration()
applyCommonArtifactoryConfig()

repositories {
    maven {
        name = "purpur"
        url = uri("https://repo.purpurmc.org/snapshots")
    }
}

val shadeOnly by configurations.creating

dependencies {
    compileOnly("org.purpurmc.purpur:purpur-api:1.19.4-R0.1-SNAPSHOT")
    api(project(":nuvotifier-api"))
    api(project(":nuvotifier-common"))
}

configurations.compileClasspath.get().extendsFrom(shadeOnly)

tasks.named<Copy>("processResources") {
    val internalVersion = project.ext["internalVersion"]
    inputs.property("internalVersion", internalVersion)
    filesMatching("plugin.yml") {
        expand("internalVersion" to internalVersion)
    }
}

tasks.named<Jar>("jar") {
    val projectVersion = project.version
    inputs.property("projectVersion", projectVersion)
    manifest {
        attributes("Implementation-Version" to projectVersion)
    }
}

tasks.withType<ShadowJar>().configureEach {
    configurations = listOf(shadeOnly, project.configurations["runtimeClasspath"])
    dependencies {
        include(dependency(":nuvotifier-api"))
        include(dependency(":nuvotifier-common"))
    }
}

tasks.named("assemble") {
    dependsOn("shadowJar")
}

tasks.register("runCopyJarScript", Exec::class) {
    group = "build"
    description = "Runs the copyjar.sh script after build completion."
    workingDir(rootDir)
    commandLine("sh", "copyjar.sh", project.version.toString())
}

tasks.named("build") {
    finalizedBy("runCopyJarScript")
}

