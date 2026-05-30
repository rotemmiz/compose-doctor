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

    private val sarif = """
        {
          "version": "2.1.0",
          "runs": [
            {
              "tool": { "driver": { "name": "detekt" } },
              "results": [
                { "ruleId": "RememberMissing", "level": "error",
                  "message": { "text": "remember is missing" },
                  "locations": [{ "physicalLocation": {
                    "artifactLocation": { "uri": "A.kt" }, "region": { "startLine": 3 } } }] },
                { "ruleId": "ComposableNaming", "level": "warning",
                  "message": { "text": "name should be PascalCase" },
                  "locations": [{ "physicalLocation": {
                    "artifactLocation": { "uri": "B.kt" }, "region": { "startLine": 5 } } }] }
              ]
            }
          ]
        }
    """.trimIndent()

    private fun writeProject(failBelow: Int? = null) {
        File(projectDir, "findings.sarif").writeText(sarif)
        File(projectDir, "settings.gradle.kts").writeText("""rootProject.name = "fixture"""")
        File(projectDir, "build.gradle.kts").writeText(
            """
            plugins { id("dev.composedoctor") }
            composeDoctor {
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

    @Test
    fun `aggregates SARIF into the expected score`() {
        writeProject()
        val result = runner().build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":composeDoctor")?.outcome)
        // 1 error rule + 1 warning rule -> 100 - 1.5 - 0.75 = 97.75 -> 98
        assertTrue(
            result.output.contains("health score: 98/100"),
            "expected score 98 in output:\n${result.output}",
        )
        assertTrue(result.output.contains("[GREAT]"))
        assertTrue(File(projectDir, ".compose-doctor/history.jsonl").exists())
    }

    @Test
    fun `fails the build when the score is below the gate`() {
        writeProject(failBelow = 99)
        val result = runner().buildAndFail()

        assertTrue(
            result.output.contains("is below the required minimum of 99"),
            "expected gate failure in output:\n${result.output}",
        )
    }
}
