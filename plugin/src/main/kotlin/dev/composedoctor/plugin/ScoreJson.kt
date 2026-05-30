package dev.composedoctor.plugin

import dev.composedoctor.scoring.Finding
import dev.composedoctor.scoring.Score
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

/** Machine-readable shape of a run, written to the report JSON for CI to consume. */
@Serializable
data class ScoreJson(
    val score: Int,
    val label: String,
    val uniqueErrorRules: Int,
    val uniqueWarningRules: Int,
    val totalFindings: Int,
    val dimensions: Map<String, Int>,
    val findings: List<FindingJson>,
)

@Serializable
data class FindingJson(
    val ruleId: String,
    val dimension: String,
    val severity: String,
    val file: String,
    val line: Int,
    val engine: String,
    val message: String,
)

object ScoreJsonWriter {
    private val json = Json { prettyPrint = true }

    fun toJson(score: Score, findings: List<Finding>): String {
        val dto = ScoreJson(
            score = score.overall,
            label = score.label.name,
            uniqueErrorRules = score.uniqueErrorRules,
            uniqueWarningRules = score.uniqueWarningRules,
            totalFindings = findings.size,
            dimensions = score.perDimension.entries
                .sortedBy { it.key.name }
                .associate { it.key.name to it.value },
            findings = findings
                .sortedWith(compareBy({ it.filePath }, { it.line }, { it.ruleId }))
                .map {
                    FindingJson(
                        ruleId = it.ruleId,
                        dimension = it.dimension.name,
                        severity = it.severity.name,
                        file = it.filePath,
                        line = it.line,
                        engine = it.engine,
                        message = it.message,
                    )
                },
        )
        return json.encodeToString(dto)
    }
}
