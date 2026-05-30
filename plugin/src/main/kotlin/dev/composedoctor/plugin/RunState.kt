package dev.composedoctor.plugin

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/** Persisted snapshot of the last run, used to compute the delta on the next run. */
@Serializable
data class RunState(
    val score: Int,
    val ruleIds: List<String>,
)

object RunStateStore {
    private val json = Json { prettyPrint = true }

    fun read(file: File): RunState? =
        if (file.exists()) runCatching { json.decodeFromString<RunState>(file.readText()) }.getOrNull()
        else null

    fun write(file: File, state: RunState) {
        file.parentFile?.mkdirs()
        file.writeText(json.encodeToString(state))
    }
}
