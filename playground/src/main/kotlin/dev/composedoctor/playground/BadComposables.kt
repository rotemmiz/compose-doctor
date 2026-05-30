package dev.composedoctor.playground

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel

/**
 * A deliberately broken set of composables. Each function documents the compose-rules violation
 * it is meant to trigger, so running `./gradlew -p playground composeDoctor` should report a
 * non-perfect score with these findings.
 *
 * This file is analysed by detekt (PSI, no type resolution); it is not meant to compile.
 */

// ISSUE: emits UI but is named lowercase (ComposableNaming),
// and has no Modifier parameter (ModifierMissing).
@Composable
fun myScreen() {
    Text("hello")
}

// ISSUE: the Modifier parameter has no default value (ModifierWithoutDefault).
@Composable
fun Header(modifier: Modifier) {
    Text("header", modifier)
}

// ISSUE: a required parameter follows one with a default (ComposableParamOrder).
@Composable
fun Profile(modifier: Modifier = Modifier, name: String) {
    Text(name, modifier)
}

// ISSUE: a MutableState is passed in as a parameter (MutableStateParam).
@Composable
fun Editor(state: MutableState<String>, modifier: Modifier = Modifier) {
    Text(state.value, modifier)
}

// ISSUE: mutableStateOf is not wrapped in remember (RememberMissing).
@Composable
fun Toggle(modifier: Modifier = Modifier) {
    val checked = mutableStateOf(false)
    Text(checked.value.toString(), modifier)
}

// ISSUE: a ViewModel is forwarded down into another composable (ViewModelForwarding).
@Composable
fun Container(viewModel: FeedViewModel, modifier: Modifier = Modifier) {
    Detail(viewModel, modifier)
}

@Composable
fun Detail(viewModel: FeedViewModel, modifier: Modifier = Modifier) {
    Text(viewModel.title, modifier)
}

class FeedViewModel : ViewModel() {
    val title: String = "Feed"
}
