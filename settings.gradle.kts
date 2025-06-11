rootProject.name = "Votifier-OG"

include("nuvotifier-api")
project(":nuvotifier-api").projectDir = file("api")

include("nuvotifier-common")
project(":nuvotifier-common").projectDir = file("common")

include("nuvotifier-bukkit")
project(":nuvotifier-bukkit").projectDir = file("bukkit")

include("nuvotifier-universal")
project(":nuvotifier-universal").projectDir = file("universal")
