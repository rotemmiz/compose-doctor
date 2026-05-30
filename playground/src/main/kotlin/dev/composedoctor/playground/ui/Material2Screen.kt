package dev.composedoctor.playground.ui

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// ISSUE Material2: uses Material 2 (androidx.compose.material) instead of Material 3.
@Composable
fun LegacyButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier) {
        Text("Legacy")
    }
}
