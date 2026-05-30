// Standalone build that consumes compose-doctor straight from source via a composite build.
// Run with:  ./gradlew -p playground composeDoctor
pluginManagement {
    includeBuild("..")
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}

rootProject.name = "compose-doctor-playground"
