plugins {
    java
    id("maven-publish")
}

group = "me.drownek"
version = "2.0.1"

dependencies {
    implementation("org.jetbrains:annotations:20.1.0")

    // adventure
    implementation("net.kyori:adventure-api:4.16.0")
    implementation("net.kyori:adventure-text-serializer-legacy:4.16.0")
    implementation("net.kyori:adventure-text-minimessage:4.16.0")
    implementation("net.kyori:adventure-platform-bukkit:4.2.0")

    // Spigot API
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")

    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}