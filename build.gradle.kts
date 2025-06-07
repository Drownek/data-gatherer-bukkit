plugins {
    java
    id("maven-publish")
}

group = "me.drownek"
version = "2.0"

repositories {
    mavenCentral()
    mavenLocal()
    maven { url = uri("https://jitpack.io") }
    maven {
        name = "enginehub-maven"
        url = uri("https://maven.enginehub.org/repo/")
    }
    maven {
        name = "storehouse-releases"
        url = uri("https://storehouse.okaeri.eu/repository/maven-releases/")
    }
    maven {
        name = "panda-repo"
        url = uri("https://repo.panda-lang.org/releases")
    }
    maven {
        name = "spigot-repo"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
}

dependencies {
    implementation("com.github.Drownek:bukkit-utils:1.0")
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