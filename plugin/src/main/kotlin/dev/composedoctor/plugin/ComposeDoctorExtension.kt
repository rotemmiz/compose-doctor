package dev.composedoctor.plugin

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

/** DSL configuration for the `composeDoctor { }` block. */
abstract class ComposeDoctorExtension {
    /** SARIF reports emitted by detekt / android-lint that should be aggregated into the score. */
    abstract val sarifReports: ConfigurableFileCollection

    /** Fail the build if the overall score is below this value. Unset = no gate. */
    abstract val failBelow: Property<Int>

    /** File to append per-run history to, for local trend tracking. */
    abstract val historyFile: RegularFileProperty
}
