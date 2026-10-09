package tj.umar.navoplayer.feature.player.trackactions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.icon.NavoIcons
import tj.umar.navoplayer.core.designsystem.medallion.Medallion
import tj.umar.navoplayer.core.designsystem.medallion.MedallionPalettes
import tj.umar.navoplayer.core.designsystem.theme.NavoShapes
import tj.umar.navoplayer.core.designsystem.theme.NavoSpacing
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.core.domain.model.Track
import tj.umar.navoplayer.feature.player.R
import tj.umar.navoplayer.core.ui.R as CoreUiR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TrackActionsSheet(
    state: TrackActionsState,
    sheetState: SheetState,
    onIntent: (TrackActionsIntent) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier,
        containerColor = NavoTheme.colors.raised,
        contentColor = NavoTheme.colors.content,
    ) {
        TrackActionsContent(state = state, onIntent = onIntent)
    }
}

@Composable
internal fun TrackActionsContent(state: TrackActionsState, onIntent: (TrackActionsIntent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, bottom = NavoSpacing.Large),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        TrackActionsHeader(state = state)
        val enabled = !state.isWorking
        ActionRow(
            icon = NavoIcons.SkipNext,
            label = stringResource(R.string.player_track_actions_play_next),
            enabled = enabled,
            onClick = { onIntent(TrackActionsIntent.PlayNextClicked) },
        )
        ActionRow(
            icon = NavoIcons.Queue,
            label = stringResource(R.string.player_track_actions_add_to_queue),
            enabled = enabled,
            onClick = { onIntent(TrackActionsIntent.AddToQueueClicked) },
        )
        ActionRow(
            icon = NavoIcons.Plus,
            label = stringResource(CoreUiR.string.core_ui_add_to_playlist),
            enabled = enabled,
            onClick = { onIntent(TrackActionsIntent.AddToPlaylistClicked) },
        )
    }
}

@Composable
private fun TrackActionsHeader(state: TrackActionsState) {
    val colors = NavoTheme.colors
    val track = state.track
    val title = track?.title ?: pluralStringResource(
        R.plurals.player_queue_track_count,
        state.trackIds.size,
        state.trackIds.size,
    )
    val paletteKey = track?.id ?: state.trackIds.firstOrNull() ?: 0L
    val palette = remember(paletteKey) { MedallionPalettes.forKey(paletteKey) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = NavoSpacing.Small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Medallion(palette = palette, modifier = Modifier.size(48.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = title,
                style = NavoTheme.typography.titleS,
                color = colors.content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.semantics { heading() },
            )
            if (track != null) {
                Text(
                    text = track.artist ?: stringResource(CoreUiR.string.core_ui_unknown_artist),
                    style = NavoTheme.typography.secondary,
                    color = colors.contentSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun ActionRow(icon: ImageVector, label: String, enabled: Boolean, onClick: () -> Unit) {
    val colors = NavoTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(NavoShapes.TrackRow)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = null, tint = colors.content, modifier = Modifier.size(24.dp))
        }
        Text(text = label, style = NavoTheme.typography.itemTitle, color = colors.content)
    }
}

@Preview(widthDp = 390)
@Composable
private fun TrackActionsSingleTrackPreview() {
    NavoTheme {
        Box(modifier = Modifier.background(NavoTheme.colors.raised)) {
            TrackActionsContent(
                state = TrackActionsState(
                    token = 1,
                    trackIds = listOf(2),
                    isLoading = false,
                    tracks = listOf(
                        Track(2, "Ҷавонӣ", "Daler Nazarov", null, 11, 101, 252_000, 2, "content://media/2", "Music", null, null),
                    ),
                ),
                onIntent = {},
            )
        }
    }
}

@Preview(widthDp = 390)
@Composable
private fun TrackActionsManyTracksPreview() {
    NavoTheme {
        Box(modifier = Modifier.background(NavoTheme.colors.raised)) {
            TrackActionsContent(
                state = TrackActionsState(token = 1, trackIds = listOf(1, 2, 3), isLoading = false),
                onIntent = {},
            )
        }
    }
}
