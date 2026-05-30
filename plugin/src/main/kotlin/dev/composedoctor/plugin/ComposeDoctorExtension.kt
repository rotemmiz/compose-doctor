package dev.composedoctor.plugin

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

/** DSL configuration for the `composeDoctor { }` block. */
abstract class ComposeDoctorExtension {
    /**
     * When true (default), the plugin applies detekt, attaches the compose-rules ruleset, enables
     * its SARIF report, and feeds it into the score automatically. Set false if you already
     * configure detekt yourself and only want compose-doctor to aggregate via [sarifReports].
     */
    abstract val autoConfigureDetekt: Property<Boolean>

    /** Additional SARIF reports (e.g. android-lint) to aggregate alongside the auto-wired ones. */
    abstract val sarifReports: ConfigurableFileCollection

    /** Fail the build if the overall score is below this value. Unset = no gate. */
    abstract val failBelow: Property<Int>

    /** Machine-readable score + findings report, consumed by CI and the agent harness. */
    abstract val reportJson: RegularFileProperty

    /** Persisted last-run snapshot, used to compute the delta on the next run. */
    abstract val stateFile: RegularFileProperty

    /** File to append per-run history to, for local trend tracking. */
    abstract val historyFile: RegularFileProperty
}
