import org.ajoberstar.grgit.Grgit

plugins {
    id("java") // Tell gradle this is a java project.
    id("java-library") // Import helper for source-based libraries.
    id("com.diffplug.spotless") version "7.0.4" // Import auto-formatter.
    eclipse // Import eclipse plugin for IDE integration.
    id("net.kyori.blossom") version "1.3.1"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

logger.lifecycle(
    """
*******************************************
 You are building NuVotifier!
 If you encounter trouble:
 1) Try running 'build' in a separate Gradle run
 2) Use ./gradlew and not gradle
 3) If you still need help, you should reconsider building NuVotifier!

 Output files will be in [subproject]/build/libs
*******************************************
"""
)

applyRootArtifactoryConfig()

if (!project.hasProperty("gitCommitHash")) {
    pluginManager.apply("org.ajoberstar.grgit")
    extra["gitCommitHash"] = try {
        Grgit.open(mapOf("currentDir" to project.rootDir))?.head()?.abbreviatedId
    } catch (e: Exception) {
        logger.warn("Error getting commit hash", e)
        "no.git.id"
    }
}

allprojects {
    group = "net.trueog.votifier-og"
    version = "1.0-SNAPSHOT"
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "com.diffplug.spotless")

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        toolchain { languageVersion.set(JavaLanguageVersion.of(17)) }
    }

    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://repo.purpurmc.org/snapshots") }
    }

    tasks.withType<ProcessResources>().configureEach {
        val props = mapOf("version" to project.version, "apiVersion" to "1.19")
        inputs.properties(props)
        filesMatching("plugin.yml") { expand(props) }
        from(rootProject.file("LICENSE")) { into("/") }
    }

    tasks.withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }

    spotless {
        java {
            removeUnusedImports()
            palantirJavaFormat()
        }
        kotlinGradle {
            ktfmt().kotlinlangStyle().configure { it.setMaxWidth(120) }
            target("*.gradle.kts", "settings.gradle.kts")
        }
    }

    tasks.build { dependsOn(tasks.spotlessApply) }

    plugins.withId("com.gradleup.shadow") {
        tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
            archiveClassifier.set("")
            minimize()
        }
        tasks.build { dependsOn("shadowJar") }
    }
}

