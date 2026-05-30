package dev.composedoctor.rulemap

import dev.composedoctor.scoring.Dimension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class RuleMapTest {

    @Test
    fun `known rules map to their dimension`() {
        assertEquals(Dimension.STATE_CORRECTNESS, RuleMap.dimensionFor("RememberMissing"))
        assertEquals(Dimension.PERFORMANCE, RuleMap.dimensionFor("UnstableCollections"))
        assertEquals(Dimension.ACCESSIBILITY, RuleMap.dimensionFor("ContentDescription"))
    }

    @Test
    fun `unknown rules fall back to ARCHITECTURE and report as unknown`() {
        assertEquals(Dimension.ARCHITECTURE, RuleMap.dimensionFor("SomeBrandNewRule"))
        assertFalse(RuleMap.isKnown("SomeBrandNewRule"))
    }

    @Test
    fun `unknown rules are bucketed by their detekt rule set`() {
        assertEquals(Dimension.STATE_CORRECTNESS, RuleMap.dimensionFor("UnsafeCast", "potential-bugs"))
        assertEquals(Dimension.STATE_CORRECTNESS, RuleMap.dimensionFor("SleepInsteadOfDelay", "coroutines"))
        assertEquals(Dimension.PERFORMANCE, RuleMap.dimensionFor("ForEachOnRange", "performance"))
        assertEquals(Dimension.ARCHITECTURE, RuleMap.dimensionFor("SwallowedException", "exceptions"))
    }

    @Test
    fun `a known compose rule keeps its dimension regardless of rule set`() {
        assertEquals(Dimension.STATE_CORRECTNESS, RuleMap.dimensionFor("RememberMissing", "Compose"))
    }
}
