package tj.umar.navoplayer.feature.playlists.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.component.NavoButton
import tj.umar.navoplayer.core.designsystem.component.NavoIconButton
import tj.umar.navoplayer.core.designsystem.component.NavoSummaryHorizontalPadding
import tj.umar.navoplayer.core.designsystem.icon.NavoIcons
import tj.umar.navoplayer.core.designsystem.theme.NavoSpacing
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.core.ui.R as CoreUiR
import tj.umar.navoplayer.feature.playlists.R

@Composable
internal fun CollectionHeader(title: String, artwork: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = NavoSpacing.Medium, vertical = NavoSpacing.Small),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        artwork()
        Text(
            text = title,
            style = NavoTheme.typography.displayM,
            color = NavoTheme.colors.content,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.semantics { heading() },
        )
    }
}

@Composable
internal fun CollectionSummary(
    trackCount: Int,
    totalMinutes: Int,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
) {
    val colors = NavoTheme.colors
    val typography = NavoTheme.typography
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = NavoSummaryHorizontalPadding, top = 12.dp, end = NavoSummaryHorizontalPadding, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = pluralStringResource(CoreUiR.plurals.core_ui_track_count, trackCount, trackCount),
                style = typography.titleS,
                color = colors.content,
            )
            Text(
                text = pluralStringResource(CoreUiR.plurals.core_ui_minute_count, totalMinutes, totalMinutes),
                style = typography.secondary,
                color = colors.contentSecondary,
            )
        }
        if (trackCount > 0) {
            NavoIconButton(
                icon = NavoIcons.Shuffle,
                contentDescription = stringResource(R.string.playlists_shuffle),
                onClick = onShuffle,
                containerColor = colors.raised,
                iconSize = 22.dp,
            )
            NavoButton(
                text = stringResource(if (isPlaying) R.string.playlists_pause else R.string.playlists_play),
                onClick = onPlay,
                leadingIcon = if (isPlaying) NavoIcons.Pause else NavoIcons.Play,
                height = NavoSpacing.MinTouchTarget,
                contentPadding = PaddingValues(start = 14.dp, end = 18.dp),
                textStyle = typography.label.copy(fontWeight = FontWeight.SemiBold),
            )
        }
    }
}

@Composable
internal fun CollectionEmptyNote(title: String, hint: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = NavoSpacing.Large, vertical = NavoSpacing.Large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = title,
            style = NavoTheme.typography.itemTitle,
            color = NavoTheme.colors.content,
            textAlign = TextAlign.Center,
        )
        Text(
            text = hint,
            style = NavoTheme.typography.secondary,
            color = NavoTheme.colors.contentSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
internal fun UnavailableTracksNote(count: Int) {
    Text(
        text = pluralStringResource(R.plurals.playlists_unavailable_count, count, count),
        style = NavoTheme.typography.caption,
        color = NavoTheme.colors.contentMuted,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = NavoSpacing.Medium, vertical = NavoSpacing.Medium),
    )
}
