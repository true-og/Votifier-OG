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

configurations.compileClasspath.get().extendsFrom(shadeOnly)

dependencies {
    compileOnly("org.purpurmc.purpur:purpur-api:1.19.4-R0.1-SNAPSHOT")
    api(project(":nuvotifier-api"))
    api(project(":nuvotifier-common"))
}

java { toolchain.languageVersion.set(JavaLanguageVersion.of(17)) }

tasks.named<Copy>("processResources") {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    val internalVersion = project.extra["internalVersion"]
    inputs.property("internalVersion", internalVersion)
    filesMatching("plugin.yml") { expand("internalVersion" to internalVersion) }
    from(rootProject.file("LICENSE")) { into("/") }
}

tasks.named<Jar>("jar") { manifest { attributes("Implementation-Version" to project.version) } }

tasks.withType<ShadowJar>().configureEach {
    configurations = listOf(shadeOnly, project.configurations["runtimeClasspath"])
    dependencies {
        include(dependency(":nuvotifier-api"))
        include(dependency(":nuvotifier-common"))
    }
    exclude("LICENSE*")
    archiveClassifier.set("")
    minimize()
}

tasks.named("assemble") { dependsOn("shadowJar") }

tasks.register<Exec>("runCopyJarScript") {
    group = "build"
    description = "Runs the copyjar.sh script after build completion."
    workingDir(rootDir)
    commandLine("sh", "copyjar.sh", project.version.toString())
}

tasks.named("build") { finalizedBy("runCopyJarScript") }
