package dev.composedoctor.playground.ui.state

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

// ISSUE MutableStateParam: a MutableState is passed in as a parameter.
@Composable
fun NameEditor(state: MutableState<String>, modifier: Modifier = Modifier) {
    Text(state.value, modifier)
}

// ISSUE RememberMissing: mutableStateOf is created without remember.
@Composable
fun Toggle(modifier: Modifier = Modifier) {
    val checked = mutableStateOf(false)
    Text(checked.value.toString(), modifier)
}

// ISSUE MutableStateAutoboxing: a boxed Int state instead of mutableIntStateOf.
@Composable
fun Counter(modifier: Modifier = Modifier) {
    val count = remember { mutableStateOf(0) }
    Text(count.value.toString(), modifier)
}
