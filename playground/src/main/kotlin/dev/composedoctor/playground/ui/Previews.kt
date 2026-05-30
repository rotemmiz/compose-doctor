package dev.composedoctor.playground.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.composedoctor.playground.ui.components.ProfileCard

// ISSUE PreviewPublic: @Preview composables should be private.
@Preview
@Composable
fun ProfileCardPreview() {
    ProfileCard(name = "Ada", bio = "Engineer")
}
