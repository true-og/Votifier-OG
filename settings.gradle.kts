rootProject.name = "Votifier-OG"

ProcessBuilder("sh", "bootstrap.sh").directory(rootDir).inheritIO().start().let {
    if (it.waitFor() != 0) throw GradleException("bootstrap.sh failed")
}

file("libs")
    .listFiles()
    ?.filter { it.isDirectory && !it.name.startsWith(".") }
    ?.forEach { dir ->
        include(":libs:${dir.name}")
        project(":libs:${dir.name}").projectDir = dir
    }

include(":nuvotifier-api", ":nuvotifier-common", ":nuvotifier-bukkit")

project(":nuvotifier-api").projectDir = file("api")

project(":nuvotifier-common").projectDir = file("common")

project(":nuvotifier-bukkit").projectDir = file("bukkit")
