package app.allever.android.lib.core.ext

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.EmptyCoroutineContext

inline fun <T> Flow<T>.launchAndCollectIn(
    owner: LifecycleOwner,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline action: suspend CoroutineScope.(T) -> Unit
) = owner.lifecycleScope.launch {
    owner.repeatOnLifecycle(minActiveState) {
        collect {
            action(it)
        }
    }
}

fun <T> MutableSharedFlow<T>.update(
    value: T,
    scope: CoroutineScope = CoroutineScope(EmptyCoroutineContext)
) {
    scope.launch {
        this@update.emit(value)
    }
}

fun <T> Channel<T>.update(value: T, scope: CoroutineScope = CoroutineScope(EmptyCoroutineContext)) {
    scope.launch {
        this@update.send(value)
    }
}