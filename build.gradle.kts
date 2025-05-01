plugins {
    java
    id("org.spongepowered.gradle.plugin") version "2.3.0"
    id("io.github.goooler.shadow") version "8.1.7"
}

group = "org.lorewriter"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("org.spongepowered:spongeapi:14.0.0-SNAPSHOT")
    compileOnly("org.slf4j:slf4j-api:2.0.13")
    compileOnly("org.spongepowered:configurate-hocon:4.1.2")
}


sponge {
    apiVersion("14.0.0-SNAPSHOT")
    license("All-Rights-Reserved")
    loader {
        name(org.spongepowered.gradle.plugin.config.PluginLoaders.JAVA_PLAIN)
        version("1.0")
    }
    plugin("lorewriter") {
        displayName("LoreWriter")
        entrypoint("org.lorewriter.lorewriter.Lorewriter")
        description("Plugin for logging and managing block events.")
        dependency("spongeapi") {
            loadOrder(org.spongepowered.plugin.metadata.model.PluginDependency.LoadOrder.AFTER)
            optional(false)
        }
        contributor("Elfieray") {
            description("Lead developer")
        }
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveClassifier.set("")
    isZip64 = true
    mergeServiceFiles()
    exclude("org/slf4j/**")
}

