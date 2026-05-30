// The compose-doctor plugin (resolved from the included build at ../) applies detekt +
// compose-rules and wires the score. No Kotlin plugin is needed: detekt analyses the source
// in src/main/kotlin directly, so this module intentionally does not compile against Compose —
// it exists purely to be diagnosed.
plugins {
    id("dev.composedoctor")
}

composeDoctor {
    // Surface the score but don't fail this demo build.
    // failBelow.set(75)
}
