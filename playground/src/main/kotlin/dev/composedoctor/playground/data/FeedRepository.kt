package dev.composedoctor.playground.data

import java.util.* // ISSUE WildcardImport (detekt)

/**
 * A deliberately-messy data layer, here to trip detekt's general code-quality rules (not just the
 * Compose ruleset) so the playground scores like a genuinely flawed codebase.
 */
class FeedRepository {

    // TODO: replace with a real data source   // ISSUE ForbiddenComment (detekt)

    private val cache = HashMap<Int, String>()

    // ISSUE ReturnCount + MagicNumber (detekt)
    fun load(id: Int): String {
        if (id < 0) return "invalid"
        if (id == 0) return "empty"
        if (id > 1000) return "too-big"
        return "item-$id"
    }

    // ISSUE ComplexCondition + NestedBlockDepth (detekt)
    fun classify(a: Boolean, b: Boolean, c: Boolean, d: Boolean): Int {
        if (a) {
            if (b) {
                if (c) {
                    if (a && b && c && d) {
                        return 1
                    }
                }
            }
        }
        return 0
    }

    // ISSUE TooGenericExceptionCaught + SwallowedException (detekt)
    fun risky(): String {
        return try {
            compute()
        } catch (e: Exception) {
            "fallback"
        }
    }

    // ISSUE EmptyFunctionBlock (detekt)
    fun reset() {}

    // ISSUE ThrowingExceptionsWithoutMessageOrCause (detekt)
    fun boom() {
        throw RuntimeException()
    }

    // ISSUE LoopWithTooManyJumpStatements (detekt)
    private fun compute(): String {
        for (i in 0..10) {
            if (i == 2) continue
            if (i == 5) break
        }
        return "ok"
    }
}
