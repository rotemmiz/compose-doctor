// compose-doctor — zero-touch init script.
//
// Applies the `dev.composedoctor` plugin to every project in a build WITHOUT editing any
// build.gradle(.kts). This is the `npx react-doctor` equivalent: run the health check against a
// repo you haven't set up.
//
//   ./gradlew --init-script compose-doctor.init.gradle.kts composeDoctor
//
// Resolution: the plugin is pulled from the Gradle Plugin Portal. This works once compose-doctor
// is published there. Until then, consume the plugin from source (composite build) — see the
// project README "Use it in your project".
//
// Pin the version to keep scores comparable across runs (the score depends on the ruleset version).

initscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath("dev.composedoctor:dev.composedoctor.gradle.plugin:0.1.0")
    }
}

allprojects {
    apply(plugin = "dev.composedoctor")
}
