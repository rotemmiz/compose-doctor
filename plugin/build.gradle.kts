plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.plugin.publish)
    `java-gradle-plugin`
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.detekt.gradle.plugin)
    testImplementation(libs.junit.jupiter)
    testImplementation(gradleTestKit())
}

gradlePlugin {
    website = "https://github.com/rotemmiz/compose-doctor"
    vcsUrl = "https://github.com/rotemmiz/compose-doctor"
    plugins {
        create("composeDoctor") {
            id = "dev.composedoctor"
            implementationClass = "dev.composedoctor.plugin.ComposeDoctorPlugin"
            displayName = "compose-doctor"
            description = "Deterministic 0-100 health score and agent fix-loop for Jetpack " +
                "Compose, wrapping detekt + compose-rules."
            tags = listOf(
                "android",
                "compose",
                "jetpack-compose",
                "detekt",
                "static-analysis",
                "code-quality",
            )
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

// Add license + developer metadata to the published POM (plugin-publish generates the rest).
afterEvaluate {
    publishing {
        publications.withType<org.gradle.api.publish.maven.MavenPublication>().configureEach {
            pom {
                licenses {
                    license {
                        name = "MIT"
                        url = "https://opensource.org/licenses/MIT"
                    }
                }
                developers {
                    developer {
                        id = "rotemmiz"
                        name = "Rotem Meidan"
                    }
                }
            }
        }
    }
}
