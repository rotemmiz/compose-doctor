rootProject.name = "compose-doctor"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}

include("scoring", "rule-map", "plugin")
