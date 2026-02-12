package com.arnoagape.polyscribe.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Collects events from a [Flow] and executes the given callback for each event.
 *
 * This effect is lifecycle-aware and collects events only while the
 * [LocalLifecycleOwner] is in the [Lifecycle.State.STARTED] state.
 *
 * @param flow Flow emitting events from the ViewModel.
 * @param onEvent Callback invoked for each emitted event.
 */
@Composable
fun <T> EventsEffect(flow: Flow<T>, onEvent: suspend (T) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnEvent by rememberUpdatedState(onEvent)

    LaunchedEffect(flow, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(Dispatchers.Main.immediate) {
                flow.collect(currentOnEvent)
            }
        }
    }
}