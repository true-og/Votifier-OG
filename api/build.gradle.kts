plugins {
    `java-library`
}

applyPlatformAndCoreConfiguration()
applyCommonArtifactoryConfig()

dependencies {
    implementation("com.google.code.gson:gson:${Versions.GSON}")
    testImplementation("junit:junit:4.13.2")
}

tasks.test {
    useJUnit()
}
