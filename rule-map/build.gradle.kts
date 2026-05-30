plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    api(project(":scoring"))
    testImplementation(libs.junit.jupiter)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
