package dev.composedoctor.playground.ui.theme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// ISSUE CompositionLocalNaming: a CompositionLocal should be prefixed with `Local`.
// ISSUE CompositionLocalAllowlist: defining a CompositionLocal outside the allowlist.
val Spacing = staticCompositionLocalOf<Int> { error("no Spacing provided") }

// ISSUE ModifierComposable: a Modifier factory should not be @Composable.
@Composable
fun Modifier.shimmer(): Modifier {
    return this
}

// ISSUE ModifierClickableOrder: clickable should come after sizing/padding in the chain.
fun Modifier.badOrder(): Modifier =
    this.clickable { }.padding(8.dp)
