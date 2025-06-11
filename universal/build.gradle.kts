import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    eclipse
    id("com.gradleup.shadow")
}

applyPlatformAndCoreConfiguration()

val shadeOnly = configurations.findByName("shadeOnly") ?: configurations.create("shadeOnly")
configurations.compileClasspath.get().extendsFrom(shadeOnly)

dependencies {
    implementation(project(":nuvotifier-api"))
    implementation(project(":nuvotifier-common"))
    implementation(project(":nuvotifier-bukkit"))
}

tasks.named<Jar>("jar") {
    manifest {
        attributes("Implementation-Version" to project.version)
    }
}

tasks.withType<ShadowJar>().configureEach {
    configurations = listOf(shadeOnly, project.configurations["runtimeClasspath"])
    dependencies {
        include(dependency(":nuvotifier-api"))
        include(dependency(":nuvotifier-common"))
        include(dependency(":nuvotifier-bukkit"))
    }
    exclude("GradleStart**")
    exclude(".cache")
    exclude("LICENSE*")
    exclude("META-INF/services/**")
    exclude("META-INF/maven/**")
    exclude("META-INF/versions/**")
    exclude("org/intellij/**")
    exclude("org/jetbrains/**")
    exclude("**/module-info.class")
}

tasks.named("assemble") {
    dependsOn("shadowJar")
}

