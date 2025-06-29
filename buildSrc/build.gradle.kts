plugins {
    `kotlin-dsl`
    kotlin("jvm") version embeddedKotlinVersion
    id("com.gradleup.shadow") version "8.3.6"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven {
        name = "sponge"
        url = uri("https://repo.spongepowered.org/repository/maven-public/")
    }
}

dependencies {
    implementation(gradleApi())
    implementation("org.ajoberstar.grgit:grgit-gradle:4.1.1")
    implementation("org.jfrog.buildinfo:build-info-extractor-gradle:4.27.1")
    implementation("com.gradleup.shadow:shadow-gradle-plugin:8.3.6")
    implementation("org.spongepowered:spongegradle-plugin-development:2.0.0")
}

