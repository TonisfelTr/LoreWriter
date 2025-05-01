rootProject.name = "LoreWriter"

pluginManagement {
    repositories {
        // Стандартный репозиторий плагинов Gradle
        gradlePluginPortal()
        // Репозиторий Sponge, где лежит SpongeGradle
        maven("https://repo.spongepowered.org/repository/maven-public/")
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "org.spongepowered.gradle.plugin") {
                // Важно: координаты org.spongepowered.gradle.plugin.gradle.plugin:<версия>
                useModule("org.spongepowered.gradle.plugin:org.spongepowered.gradle.plugin.gradle.plugin:${requested.version}")
            }
        }
    }
}

// (Не обязательно, но удобно для управления зависимостями)
// Начиная с Gradle 7 можно настроить global repositories так:
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://repo.spongepowered.org/repository/maven-public/")
    }
}
