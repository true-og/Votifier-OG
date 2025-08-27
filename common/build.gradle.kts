plugins { `java-library` }

applyPlatformAndCoreConfiguration()

applyCommonArtifactoryConfig()

dependencies {
    api(project(":nuvotifier-api"))
    implementation("io.netty:netty-handler:${Versions.NETTYIO}")
    implementation("io.netty:netty-transport-native-epoll:${Versions.NETTYIO}:linux-x86_64")
    implementation("com.google.code.gson:gson:${Versions.GSON}")
}
