import org.gradle.api.initialization.resolve.RepositoriesMode

rootProject.name = "amazon-price-watcher"
include("app")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        mavenCentral()
    }
}