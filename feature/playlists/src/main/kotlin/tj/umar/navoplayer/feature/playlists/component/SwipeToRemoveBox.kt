package tj.umar.navoplayer.feature.playlists.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.icon.NavoIcons
import tj.umar.navoplayer.core.designsystem.theme.NavoShapes
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme

@Composable
internal fun SwipeToRemoveBox(
    enabled: Boolean,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = NavoTheme.colors
    val dismissState = rememberSwipeToDismissBoxState()
    LaunchedEffect(dismissState) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) dismissState.snapTo(SwipeToDismissBoxValue.Settled)
    }
    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        gesturesEnabled = enabled,
        onDismiss = { value -> if (value == SwipeToDismissBoxValue.EndToStart) onRemove() },
        backgroundContent = {
            if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.crimson, NavoShapes.TrackRow)
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = NavoIcons.Delete,
                        contentDescription = null,
                        tint = colors.onTile,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        },
    ) {
        content()
    }
}
