package dev.composedoctor.playground.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// ISSUE ModifierNaming: the Modifier parameter is not named `modifier`.
@Composable
fun ProfileCard(mod: Modifier = Modifier, name: String, bio: String) {
    Column(mod) {
        Text(name)
        Text(bio)
    }
}

// ISSUE ModifierWithoutDefault: the Modifier parameter has no default value.
@Composable
fun SectionHeader(modifier: Modifier) {
    Text("Header", modifier)
}

// ISSUE ComposableParamOrder: a required parameter follows one with a default.
@Composable
fun Badge(modifier: Modifier = Modifier, label: String) {
    Text(label, modifier)
}

// ISSUE ModifierReused: the same modifier is applied to multiple elements.
@Composable
fun TwoLines(modifier: Modifier = Modifier) {
    Column {
        Text("one", modifier.padding(4.dp))
        Text("two", modifier.padding(8.dp))
    }
}

// ISSUE MultipleEmitters: emits more than one element at the root without a layout.
@Composable
fun Duo(modifier: Modifier = Modifier) {
    Text("left")
    Text("right")
}

// ISSUE ContentEmitterReturningValues: emits content and also returns a value.
// ISSUE MagicNumber (detekt): the literal 42.
@Composable
fun CounterLabel(modifier: Modifier = Modifier): Int {
    Text("count", modifier)
    return 42
}
