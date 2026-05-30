package dev.composedoctor.plugin

import dev.composedoctor.rulemap.RuleMap
import dev.composedoctor.scoring.Finding
import dev.composedoctor.scoring.Score
import dev.composedoctor.scoring.Severity
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.net.URI
import java.security.MessageDigest

/** The machine contract the agent harness reads. See docs/AGENT-HARNESS.md. */
@Serializable
data class ScoreJson(
    val schemaVersion: Int,
    val rulesetVersion: String,
    val status: String, // "ok" | "below_gate"
    val score: Int,
    val label: String,
    val uniqueErrorRules: Int,
    val uniqueWarningRules: Int,
    val totalFindings: Int,
    val dimensions: Map<String, Int>,
    val delta: DeltaJson? = null,
    val byRule: List<RuleSummaryJson>,
    val findings: List<FindingJson>,
)

@Serializable
data class DeltaJson(
    val vs: String,
    val score: Int,
    val newRules: List<String>,
    val fixedRules: List<String>,
)

/** One row of the remediation plan: clear every instance of [ruleId] to gain [scoreImpactIfCleared]. */
@Serializable
data class RuleSummaryJson(
    val ruleId: String,
    val severity: String,
    val dimension: String,
    val count: Int,
    val scoreImpactIfCleared: Double,
    val autoFixable: Boolean,
    val docsUrl: String? = null,
    val fixHint: String? = null,
)

@Serializable
data class FindingJson(
    val id: String,
    val ruleId: String,
    val dimension: String,
    val severity: String,
    val file: String,
    val line: Int,
    val engine: String,
    val message: String,
)

const val SCHEMA_VERSION = 1

object ScoreModel {
    private val json = Json { prettyPrint = true; encodeDefaults = true }

    fun build(
        score: Score,
        findings: List<Finding>,
        rulesetVersion: String,
        status: String,
        previous: RunState?,
        projectDir: File,
    ): ScoreJson {
        val byRule = findings
            .groupBy { it.ruleId }
            .map { (ruleId, fs) ->
                val severity = fs.minOf { it.severity } // ERROR has the lowest ordinal = most severe
                val info = RuleMap.infoFor(ruleId)
                RuleSummaryJson(
                    ruleId = ruleId,
                    severity = severity.name,
                    // Use the resolved dimension from the findings (ruleSet-aware), not the
                    // rule-id-only fallback in infoFor.
                    dimension = fs.first().dimension.name,
                    count = fs.size,
                    scoreImpactIfCleared = scoreImpact(severity),
                    autoFixable = info.autoFixable,
                    docsUrl = info.docsUrl,
                    fixHint = info.fixHint,
                )
            }
            .sortedWith(
                compareByDescending<RuleSummaryJson> { it.scoreImpactIfCleared }
                    .thenBy { it.count }
                    .thenBy { it.ruleId },
            )

        val findingsJson = findings
            .map { f ->
                val rel = relativize(f.filePath, projectDir)
                FindingJson(
                    id = fingerprint(f.ruleId, rel, f.line),
                    ruleId = f.ruleId,
                    dimension = f.dimension.name,
                    severity = f.severity.name,
                    file = rel,
                    line = f.line,
                    engine = f.engine,
                    message = f.message,
                )
            }
            .sortedWith(compareBy({ it.file }, { it.line }, { it.ruleId }))

        val currentRules = byRule.map { it.ruleId }.toSet()
        val delta = previous?.let { p ->
            DeltaJson(
                vs = "previous run",
                score = score.overall - p.score,
                newRules = (currentRules - p.ruleIds.toSet()).sorted(),
                fixedRules = (p.ruleIds.toSet() - currentRules).sorted(),
            )
        }

        return ScoreJson(
            schemaVersion = SCHEMA_VERSION,
            rulesetVersion = rulesetVersion,
            status = status,
            score = score.overall,
            label = score.label.name,
            uniqueErrorRules = score.uniqueErrorRules,
            uniqueWarningRules = score.uniqueWarningRules,
            totalFindings = findings.size,
            dimensions = score.perDimension.entries
                .sortedBy { it.key.name }
                .associate { it.key.name to it.value },
            delta = delta,
            byRule = byRule,
            findings = findingsJson,
        )
    }

    fun toJsonString(dto: ScoreJson): String = json.encodeToString(dto)

    private fun scoreImpact(severity: Severity): Double = when (severity) {
        Severity.ERROR -> 1.5
        Severity.WARNING -> 0.75
        Severity.INFO -> 0.0
    }

    /** Make a SARIF file URI (or path) relative to the project, for portable fingerprints. */
    private fun relativize(filePath: String, projectDir: File): String {
        val abs = runCatching {
            if (filePath.startsWith("file:")) File(URI(filePath)).path else File(filePath).path
        }.getOrDefault(filePath)
        val base = projectDir.path
        return if (abs.startsWith(base)) abs.removePrefix(base).trimStart('/') else abs
    }

    private fun fingerprint(ruleId: String, relPath: String, line: Int): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest("$ruleId|$relPath|$line".toByteArray())
        return digest.take(6).joinToString("") { "%02x".format(it) }
    }
}
