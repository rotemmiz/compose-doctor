package dev.composedoctor.plugin

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ComposeDoctorPluginFunctionalTest {

    @TempDir
    lateinit var projectDir: File

    private val twoRules = sarifOf(
        """{ "ruleId": "RememberMissing", "level": "error",
             "message": { "text": "remember is missing" },
             "locations": [{ "physicalLocation": {
               "artifactLocation": { "uri": "A.kt" }, "region": { "startLine": 3 } } }] }""",
        """{ "ruleId": "ComposableNaming", "level": "warning",
             "message": { "text": "name should be PascalCase" },
             "locations": [{ "physicalLocation": {
               "artifactLocation": { "uri": "B.kt" }, "region": { "startLine": 5 } } }] }""",
    )

    private val oneRule = sarifOf(
        """{ "ruleId": "ComposableNaming", "level": "warning",
             "message": { "text": "name should be PascalCase" },
             "locations": [{ "physicalLocation": {
               "artifactLocation": { "uri": "B.kt" }, "region": { "startLine": 5 } } }] }""",
    )

    private fun sarifOf(vararg results: String) = """
        { "version": "2.1.0", "runs": [ {
            "tool": { "driver": { "name": "detekt" } },
            "results": [ ${results.joinToString(",")} ] } ] }
    """.trimIndent()

    private fun writeProject(failBelow: Int? = null, sarif: String = twoRules) {
        File(projectDir, "findings.sarif").writeText(sarif)
        File(projectDir, "settings.gradle.kts").writeText("""rootProject.name = "fixture"""")
        File(projectDir, "build.gradle.kts").writeText(
            """
            plugins { id("dev.composedoctor") }
            composeDoctor {
                autoConfigureDetekt.set(false)
                sarifReports.from("findings.sarif")
                ${failBelow?.let { "failBelow.set($it)" } ?: ""}
            }
            """.trimIndent(),
        )
    }

    private fun runner() = GradleRunner.create()
        .withProjectDir(projectDir)
        .withPluginClasspath()
        .withArguments("composeDoctor")

    private val reportJson get() = File(projectDir, "build/reports/compose-doctor/score.json")

    @Test
    fun `aggregates SARIF into the expected score and machine report`() {
        writeProject()
        val result = runner().build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":composeDoctor")?.outcome)
        // 1 error rule + 1 warning rule -> 100 - 1.5 - 0.75 = 97.75 -> 98
        assertTrue(result.output.contains("health score: 98/100"), result.output)
        assertTrue(result.output.contains("[GREAT]"))
        assertTrue(File(projectDir, ".compose-doctor/history.jsonl").exists())

        val json = reportJson.readText()
        assertTrue(reportJson.exists(), "expected score.json to be written")
        assertTrue(json.contains("\"schemaVersion\": 1"))
        assertTrue(json.contains("\"status\": \"ok\""))
        assertTrue(json.contains("\"score\": 98"))
        assertTrue(json.contains("\"byRule\""))
        assertTrue(json.contains("\"RememberMissing\""))
        // error rule (1.5) ranks above the warning rule (0.75) in the plan
        assertTrue(
            json.indexOf("\"RememberMissing\"") < json.indexOf("\"ComposableNaming\""),
            "error rule should be ordered before warning rule in byRule",
        )
    }

    @Test
    fun `reports a delta against the previous run`() {
        writeProject(sarif = twoRules)
        runner().build() // first run: no previous state, no delta

        writeProject(sarif = oneRule) // fix RememberMissing
        val result = runner().build()

        // 98 -> 99 (one warning rule remains): +1, cleared 1, new 0
        assertTrue(result.output.contains("Δ vs previous run"), result.output)
        assertTrue(result.output.contains("cleared 1, new 0"), result.output)
        assertTrue(reportJson.readText().contains("\"fixedRules\""))
    }

    @Test
    fun `fails the build when the score is below the gate`() {
        writeProject(failBelow = 99)
        val result = runner().buildAndFail()

        assertTrue(result.output.contains("is below the required minimum of 99"), result.output)
        // the report is still written, with status reflecting the gate
        assertTrue(reportJson.readText().contains("\"status\": \"below_gate\""))
    }
}
