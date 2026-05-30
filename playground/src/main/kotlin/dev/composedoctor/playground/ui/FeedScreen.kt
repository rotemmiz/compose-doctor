package dev.composedoctor.playground.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * A deliberately-flawed feed "screen". Every issue is annotated with the compose-rules rule it is
 * meant to trip. This file is analysed by detekt (PSI, no type resolution); it is not meant to
 * compile. See docs/TRY-IT-PLAYGROUND.md.
 */

// ISSUE ComposableNaming: a UI-emitting composable should be PascalCase.
// ISSUE ModifierMissing: emits content but exposes no Modifier parameter.
// ISSUE ViewModelInjection: the ViewModel is injected here instead of being hoisted to the caller.
// ISSUE UnstableCollections: List<> as a parameter can defeat recomposition skipping.
// ISSUE MutableParams: ArrayList<> is a mutable parameter type.
// ISSUE LongParameterList (detekt): too many parameters.
@Composable
fun feedScreen(
    title: String,
    subtitle: String,
    items: List<FeedItem>,
    tags: ArrayList<String>,
    isLoading: Boolean,
    showHeader: Boolean,
    onRefresh: () -> Unit,
) {
    val vm = viewModel<FeedViewModel>()
    // ISSUE LambdaParameterInRestartableEffect: a parameter lambda used in a restartable effect
    // without rememberUpdatedState captures a stale value.
    LaunchedEffect(Unit) {
        onRefresh()
    }
    Column {
        FeedList(vm, items)
    }
}

// ISSUE ViewModelForwarding: forwarding a ViewModel into a child composable.
// ISSUE ModifierMissing: emits content, no Modifier.
@Composable
fun FeedList(viewModel: FeedViewModel, items: List<FeedItem>) {
    LazyColumn {
        // intentionally empty
    }
}

data class FeedItem(val id: Int, val title: String)

class FeedViewModel : ViewModel() {
    val title: String = "Feed"
}
