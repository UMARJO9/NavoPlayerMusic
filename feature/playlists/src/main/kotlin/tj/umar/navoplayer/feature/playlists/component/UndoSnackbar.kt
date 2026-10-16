package tj.umar.navoplayer.feature.playlists.component

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
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

internal fun CoroutineScope.showUndoSnackbar(
    hostState: SnackbarHostState,
    message: String,
    actionLabel: String,
    onResult: (undo: Boolean) -> Unit,
) {
    hostState.currentSnackbarData?.dismiss()
    launch {
        val result = hostState.showSnackbar(message = message, actionLabel = actionLabel, duration = SnackbarDuration.Long)
        onResult(result == SnackbarResult.ActionPerformed)
    }
}
