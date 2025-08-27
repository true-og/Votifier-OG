import org.ajoberstar.grgit.Grgit

plugins {
    id("java")
    id("java-library")
    id("com.diffplug.spotless") version "7.0.4"
    eclipse
    id("net.kyori.blossom") version "2.1.0"
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

    tasks.withType<Test>().configureEach {
        useJUnit()
        filter { setFailOnNoMatchingTests(false) }
        onlyIf {
            fileTree(project.layout.projectDirectory.dir("src/test")).matching {
                include("**/*Test.*", "**/*Tests.*", "**/*Spec.*")
            }.files.isNotEmpty()
        }
    }
}

