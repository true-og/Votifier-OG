import org.gradle.kotlin.dsl.provideDelegate

/* ------------------------------ Plugins ------------------------------ */
plugins {
    id("java") // Import Java plugin.
    id("java-library") // Import Java Library plugin.
    id("com.diffplug.spotless") version "7.0.4" // Import Spotless plugin.
    id("com.gradleup.shadow") version "8.3.9" // Import Shadow plugin.
    id("checkstyle") // Import Checkstyle plugin.
    eclipse // Import Eclipse plugin.
    kotlin("jvm") version "2.1.21" // Import Kotlin JVM plugin.
}

extra["kotlinAttribute"] = Attribute.of("kotlin-tag", Boolean::class.javaObjectType)

val kotlinAttribute: Attribute<Boolean> by rootProject.extra

/* --------------------------- JDK / Kotlin ---------------------------- */
java {
    sourceCompatibility = JavaVersion.VERSION_17 // Compile with JDK 17 compatibility.
    toolchain { // Select Java toolchain.
        languageVersion.set(JavaLanguageVersion.of(17)) // Use JDK 17.
        vendor.set(JvmVendorSpec.GRAAL_VM) // Use GraalVM CE.
    }
}

kotlin { jvmToolchain(17) }

/* ----------------------------- Metadata ------------------------------ */
group = "net.trueog.votifier-og" // Declare bundle identifier.

version = "1.0" // Declare plugin version (will be in .jar).

val apiVersion = "1.19" // Declare minecraft server target version.

/* ----------------------------- Resources ----------------------------- */
tasks.named<ProcessResources>("processResources") {
    val props = mapOf("version" to version, "apiVersion" to apiVersion)
    inputs.properties(props) // Indicates to rerun if version changes.
    filesMatching("plugin.yml") { expand(props) }
    from("LICENSE") { into("/") } // Bundle licenses into jarfiles.
}

/* ---------------------------- Repos ---------------------------------- */
allprojects {
    repositories {
        mavenCentral() // Import the Maven Central Maven Repository.
        gradlePluginPortal() // Import the Gradle Plugin Portal Maven Repository.
        maven { url = uri("https://repo.purpurmc.org/snapshots") } // Import the PurpurMC Maven Repository.
        maven { url = uri("file://${System.getProperty("user.home")}/.m2/repository") }
        System.getProperty("SELF_MAVEN_LOCAL_REPO")?.let {
            val dir = file(it)
            if (dir.isDirectory) {
                println("Using SELF_MAVEN_LOCAL_REPO at: $it")
                maven { url = uri("file://${dir.absolutePath}") }
            } else {
                mavenLocal()
            }
        }
    }
}

/* ---------------------- Java project deps ---------------------------- */
configurations.create("shadeOnly")

dependencies { implementation(project(":nuvotifier-bukkit")) }

apply(from = "eclipse.gradle.kts") // Import eclipse classpath support script.

/* ---------------------- Reproducible jars ---------------------------- */
allprojects {
    tasks.withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
}

/* ----------------------------- Shadow -------------------------------- */
tasks.shadowJar {
    archiveBaseName.set("Votifier-OG")
    archiveClassifier.set("")
    minimize()
    exclude("LICENSE*")
}

tasks.jar { archiveClassifier.set("part") } // Applies to root jarfile only.

tasks.build { dependsOn(tasks.spotlessApply, tasks.shadowJar) } // Build depends on spotless and shadow.

/* --------------------------- Javac opts ------------------------------- */
allprojects {
    tasks.withType<JavaCompile>().configureEach {
        options.compilerArgs.add("-parameters") // Enable reflection for java code.
        options.isFork = true // Run javac in its own process.
        options.compilerArgs.add("-Xlint:deprecation") // Trigger deprecation warning messages.
        options.encoding = "UTF-8" // Use UTF-8 file encoding.
    }
}

/* ----------------------------- Auto Formatting ------------------------ */
spotless {
    java {
        eclipse().configFile("config/formatter/eclipse-java-formatter.xml") // Eclipse java formatting.
        leadingTabsToSpaces() // Convert leftover leading tabs to spaces.
        removeUnusedImports() // Remove imports that aren't being called.
    }
    kotlinGradle {
        ktfmt().kotlinlangStyle().configure { it.setMaxWidth(120) } // JetBrains Kotlin formatting.
        target("build.gradle.kts", "settings.gradle.kts") // Gradle files to format.
    }
}

checkstyle {
    toolVersion = "10.18.1" // Declare checkstyle version to use.
    configFile = file("config/checkstyle/checkstyle.xml") // Point checkstyle to config file.
    isIgnoreFailures = true // Don't fail the build if checkstyle does not pass.
    isShowViolations = true // Show the violations in any IDE with the checkstyle plugin.
}

tasks.named("compileJava") {
    dependsOn("spotlessApply") // Run spotless before compiling with the JDK.
}

tasks.named("spotlessCheck") {
    dependsOn("spotlessApply") // Run spotless before checking if spotless ran.
}

/* ------------------------------ Eclipse SHIM ------------------------- */

// This can't be put in eclipse.gradle.kts because Gradle is weird.
subprojects {
    apply(plugin = "java-library")
    apply(plugin = "eclipse")
    eclipse.project.name = "${project.name}-${rootProject.name}"
    tasks.withType<Jar>().configureEach { archiveBaseName.set("${project.name}-${rootProject.name}") }
}

/* ----------------------- Subproject configuration -------------------- */

subprojects {
    java {
        sourceCompatibility = JavaVersion.VERSION_17
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
            vendor.set(JvmVendorSpec.GRAAL_VM)
        }
    }
}

project(":nuvotifier-api") {
    dependencies {
        implementation("com.google.code.gson:gson:2.10.1")
        testImplementation("junit:junit:4.13.2")
    }
    tasks.test { useJUnit() }
}

project(":nuvotifier-common") {
    dependencies {
        api(project(":nuvotifier-api"))
        implementation(platform("io.netty:netty-bom:4.1.110.Final"))
        implementation("io.netty:netty-handler")
        implementation("io.netty:netty-transport-native-epoll:4.1.110.Final:linux-x86_64")
        implementation("com.google.code.gson:gson:2.10.1")
    }
}

project(":nuvotifier-bukkit") {
    dependencies {
        compileOnly("org.purpurmc.purpur:purpur-api:1.19.4-R0.1-SNAPSHOT")
        implementation(project(":nuvotifier-api"))
        implementation(project(":nuvotifier-common"))
    }
    tasks.named<ProcessResources>("processResources") {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        val props =
            mapOf(
                "version" to rootProject.version,
                "apiVersion" to apiVersion,
                "internalVersion" to rootProject.version,
            )
        inputs.properties(props)
        filesMatching("plugin.yml") { expand(props) }
        from(rootProject.file("LICENSE")) { into("/") }
    }
    tasks.named<Jar>("jar") { manifest { attributes("Implementation-Version" to project.version) } }
}
