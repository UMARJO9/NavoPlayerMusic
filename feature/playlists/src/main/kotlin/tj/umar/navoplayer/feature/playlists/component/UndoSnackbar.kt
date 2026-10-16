package tj.umar.navoplayer.feature.playlists.component

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import tj.umar.navoplayer.core.designsystem.component.NavoSnackbarHost
import tj.umar.navoplayer.core.designsystem.theme.NavoSpacing

@Composable
internal fun UndoSnackbarHost(
    hostState: SnackbarHostState,
    hasActivePlayback: Boolean,
    modifier: Modifier = Modifier,
) {
    val navigationBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val playerInset = if (hasActivePlayback) NavoSpacing.MiniPlayerListInset - NavoSpacing.Medium else 0.dp
    NavoSnackbarHost(
        hostState = hostState,
        modifier = modifier.padding(bottom = navigationBarBottom + playerInset),
    )
}

@Stable
internal class UndoSnackbarController(
    private val hostState: SnackbarHostState,
    private val scope: CoroutineScope,
) {
    private var activeJob: Job? = null

    fun show(message: String, actionLabel: String, onResult: (undo: Boolean) -> Unit) {
        activeJob?.cancel()
        hostState.currentSnackbarData?.dismiss()
        activeJob = scope.launch {
            val result = hostState.showSnackbar(message = message, actionLabel = actionLabel, duration = SnackbarDuration.Long)
            onResult(result == SnackbarResult.ActionPerformed)
        }
    }
}

@Composable
internal fun rememberUndoSnackbarController(hostState: SnackbarHostState): UndoSnackbarController {
    val scope = rememberCoroutineScope()
    return remember(hostState, scope) { UndoSnackbarController(hostState, scope) }
}
