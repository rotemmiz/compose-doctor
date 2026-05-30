package dev.composedoctor.plugin

import dev.composedoctor.scoring.Dimension
import dev.composedoctor.scoring.Finding
import dev.composedoctor.scoring.Severity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class EngineFilterTest {

    private fun finding(ruleSet: String?, severity: Severity) =
        Finding("R", Dimension.ARCHITECTURE, severity, "A.kt", 1, "m", "detekt", ruleSet)

    private val sample = listOf(
        finding("Compose", Severity.ERROR),
        finding("Compose", Severity.WARNING),
        finding("potential-bugs", Severity.ERROR),
        finding("style", Severity.WARNING),
        finding("style", Severity.INFO),
    )

    private fun ruleSetsKept(detekt: EngineLevel, compose: EngineLevel) =
        EngineFilter.keep(sample, detekt, compose).map { it.ruleSet to it.severity }

    @Test
    fun `errors and warnings keeps both, drops info`() {
        val kept = ruleSetsKept(EngineLevel.ERRORS_AND_WARNINGS, EngineLevel.ERRORS_AND_WARNINGS)
        assertEquals(4, kept.size) // the INFO one is dropped
    }

    @Test
    fun `errors-only keeps only error severity`() {
        val kept = EngineFilter.keep(sample, EngineLevel.ERRORS, EngineLevel.ERRORS)
        assertEquals(2, kept.size)
        assertEquals(listOf(Severity.ERROR, Severity.ERROR), kept.map { it.severity })
    }

    @Test
    fun `off excludes only that engine`() {
        // detekt off, compose errors+warnings -> only the two Compose findings remain
        val kept = EngineFilter.keep(sample, EngineLevel.OFF, EngineLevel.ERRORS_AND_WARNINGS)
        assertEquals(listOf("Compose", "Compose"), kept.map { it.ruleSet })
    }

    @Test
    fun `levels are independent per engine`() {
        // detekt errors-only, compose errors+warnings
        val kept = EngineFilter.keep(sample, EngineLevel.ERRORS, EngineLevel.ERRORS_AND_WARNINGS)
        // compose: error+warning (2); detekt: only error (potential-bugs) (1)
        assertEquals(3, kept.size)
    }
}
