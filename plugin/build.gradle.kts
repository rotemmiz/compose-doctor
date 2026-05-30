plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    `java-gradle-plugin`
}

dependencies {
    implementation(project(":scoring"))
    implementation(project(":rule-map"))
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.detekt.gradle.plugin)
    testImplementation(libs.junit.jupiter)
    testImplementation(gradleTestKit())
}

gradlePlugin {
    plugins {
        create("composeDoctor") {
            id = "dev.composedoctor"
            implementationClass = "dev.composedoctor.plugin.ComposeDoctorPlugin"
            displayName = "compose-doctor"
            description = "Deterministic health score and agent fix-loop for Jetpack Compose"
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
