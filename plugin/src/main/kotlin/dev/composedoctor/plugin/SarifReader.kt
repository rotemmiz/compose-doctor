package dev.composedoctor.plugin

import dev.composedoctor.rulemap.RuleMap
import dev.composedoctor.scoring.Finding
import dev.composedoctor.scoring.Severity
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File

/**
 * Minimal SARIF 2.1.0 reader: extracts ruleId, severity, location, and message from each
 * result. Engine-agnostic, so it works for detekt, android-lint, or anything else emitting
 * SARIF. Severity maps from the SARIF `level` field; the dimension is resolved via [RuleMap].
 */
object SarifReader {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun read(file: File): List<Finding> {
        val root = json.parseToJsonElement(file.readText()).jsonObject
        val runs = root["runs"]?.jsonArray ?: return emptyList()
        return runs.flatMap { runEl ->
            val run = runEl.jsonObject
            val engine = run["tool"]?.jsonObject
                ?.get("driver")?.jsonObject
                ?.get("name")?.jsonPrimitive?.contentOrNull
                ?: "unknown"
            val results = run["results"]?.jsonArray ?: return@flatMap emptyList<Finding>()
            results.mapNotNull { parseResult(it.jsonObject, engine) }
        }
    }

    private fun parseResult(result: kotlinx.serialization.json.JsonObject, engine: String): Finding? {
        // SARIF rule IDs are namespaced, e.g. "detekt.Compose.RememberMissing" — the RuleMap and
        // the report key on the bare rule name.
        val ruleId = result["ruleId"]?.jsonPrimitive?.contentOrNull?.substringAfterLast('.')
            ?: return null
        val severity = when (result["level"]?.jsonPrimitive?.contentOrNull) {
            "error" -> Severity.ERROR
            "warning" -> Severity.WARNING
            else -> Severity.INFO
        }
        val message = result["message"]?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull.orEmpty()
        val physical = result["locations"]?.jsonArray?.firstOrNull()?.jsonObject
            ?.get("physicalLocation")?.jsonObject
        val uri = physical?.get("artifactLocation")?.jsonObject
            ?.get("uri")?.jsonPrimitive?.contentOrNull.orEmpty()
        val line = physical?.get("region")?.jsonObject
            ?.get("startLine")?.jsonPrimitive?.intOrNull ?: 0

        return Finding(
            ruleId = ruleId,
            dimension = RuleMap.dimensionFor(ruleId),
            severity = severity,
            filePath = uri,
            line = line,
            message = message,
            engine = engine,
        )
    }
}
