package tj.umar.navoplayer.feature.library.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import tj.umar.navoplayer.core.designsystem.component.TrackRow
import tj.umar.navoplayer.core.designsystem.medallion.MedallionPalettes
import tj.umar.navoplayer.core.domain.model.Track
import tj.umar.navoplayer.core.ui.format.formatDuration
import tj.umar.navoplayer.core.ui.R as CoreUiR

@Composable
internal fun TrackListItem(
    track: Track,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = remember(track.id) { MedallionPalettes.forKey(track.id) }
    TrackRow(
        title = track.title.ifBlank { stringResource(CoreUiR.string.core_ui_unknown_title) },
        artist = track.artist ?: stringResource(CoreUiR.string.core_ui_unknown_artist),
        duration = formatDuration(track.durationMs),
        palette = palette,
        isCurrent = isCurrent,
        isPlaying = isCurrent && isPlaying,
        onClick = onClick,
        modifier = modifier,
    )
}
