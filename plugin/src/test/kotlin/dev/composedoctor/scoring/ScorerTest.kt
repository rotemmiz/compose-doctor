package dev.composedoctor.scoring

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ScorerTest {

    private fun finding(
        rule: String,
        sev: Severity = Severity.ERROR,
        dim: Dimension = Dimension.PERFORMANCE,
    ) = Finding(rule, dim, sev, "Foo.kt", 1, "msg", "detekt")

    @Test
    fun `clean codebase scores 100 and is GREAT`() {
        val s = Scorer.score(emptyList())
        assertEquals(100, s.overall)
        assertEquals(ScoreLabel.GREAT, s.label)
    }

    @Test
    fun `weights match the React Doctor formula`() {
        // 100 - 1*1.5 - 1*0.75 = 97.75 -> 98
        val findings = listOf(finding("A", Severity.ERROR), finding("B", Severity.WARNING))
        assertEquals(98, Scorer.score(findings).overall)
    }

    @Test
    fun `score counts unique rules, not instances`() {
        val one = listOf(finding("A", Severity.ERROR))
        val many = List(50) { finding("A", Severity.ERROR) }
        assertEquals(Scorer.score(one).overall, Scorer.score(many).overall)
    }

    @Test
    fun `clearing the last instance of a rule raises the score`() {
        val withRule = List(3) { finding("A", Severity.ERROR) }
        assertTrue(Scorer.score(emptyList()).overall > Scorer.score(withRule).overall)
    }

    @Test
    fun `scoring is order independent (deterministic)`() {
        val a = listOf(
            finding("A", Severity.ERROR),
            finding("B", Severity.WARNING),
            finding("C", Severity.ERROR),
        )
        assertEquals(Scorer.score(a), Scorer.score(a.reversed()))
    }

    @Test
    fun `a rule is penalised once, by its most severe finding`() {
        val findings = listOf(finding("A", Severity.ERROR), finding("A", Severity.WARNING))
        val s = Scorer.score(findings)
        assertEquals(1, s.uniqueErrorRules)
        assertEquals(0, s.uniqueWarningRules)
    }

    @Test
    fun `per-dimension sub-scores use the same formula`() {
        val findings = listOf(
            finding("A", Severity.ERROR, Dimension.PERFORMANCE),
            finding("B", Severity.ERROR, Dimension.ACCESSIBILITY),
        )
        val s = Scorer.score(findings)
        assertEquals(99, s.perDimension[Dimension.PERFORMANCE]) // 100 - 1.5 = 98.5 -> 99
        assertEquals(99, s.perDimension[Dimension.ACCESSIBILITY])
        assertEquals(100, s.perDimension[Dimension.STATE_CORRECTNESS])
        assertEquals(97, s.overall) // 100 - 2*1.5 = 97
    }
}
