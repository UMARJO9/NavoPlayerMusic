package tj.umar.navoplayer.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.equalizer.EqualizerBars
import tj.umar.navoplayer.core.designsystem.medallion.Medallion
import tj.umar.navoplayer.core.designsystem.medallion.MedallionPalette
import tj.umar.navoplayer.core.designsystem.medallion.MedallionPalettes
import tj.umar.navoplayer.core.designsystem.theme.NavoShapes
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.core.designsystem.R

private const val CURRENT_ROW_FADE_MILLIS = 220

@Composable
fun TrackRow(
    title: String,
    artist: String,
    duration: String,
    palette: MedallionPalette,
    modifier: Modifier = Modifier,
    isCurrent: Boolean = false,
    isPlaying: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
    onLongClickLabel: String? = null,
) {
    val colors = NavoTheme.colors
    val typography = NavoTheme.typography
    val background by animateColorAsState(
        targetValue = if (isCurrent) colors.raised else Color.Transparent,
        animationSpec = tween(CURRENT_ROW_FADE_MILLIS),
        label = "trackRowBackground",
    )
    val playLabel = stringResource(R.string.designsystem_play_track)
    val nowPlaying = stringResource(R.string.designsystem_now_playing)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .semantics(mergeDescendants = true) {
                if (isCurrent) stateDescription = nowPlaying
                if (onLongClick != null && onLongClickLabel != null) {
                    customActions = listOf(
                        CustomAccessibilityAction(onLongClickLabel) {
                            onLongClick()
                            true
                        },
                    )
                }
            }
            .clip(NavoShapes.TrackRow)
            .background(background)
            .combinedClickable(
                onClickLabel = playLabel,
                onLongClickLabel = onLongClickLabel,
                onLongClick = onLongClick,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Medallion(palette = palette, modifier = Modifier.size(48.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = title,
                style = typography.itemTitle,
                color = if (isCurrent) colors.accent else colors.content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = artist,
                style = typography.secondary,
                color = colors.contentSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Box(modifier = Modifier.widthIn(min = 36.dp), contentAlignment = Alignment.CenterEnd) {
            if (isCurrent) {
                EqualizerBars(paused = !isPlaying)
            } else {
                Text(text = duration, style = typography.secondaryNumeric, color = colors.contentSecondary)
            }
        }
    }
}

@Preview(widthDp = 390, heightDp = 200)
@Composable
private fun TrackRowCurrentPreview() {
    NavoTheme {
        Column(modifier = Modifier.background(NavoTheme.colors.background).padding(8.dp)) {
            TrackRow(
                title = "Ҷавонӣ",
                artist = "Daler Nazarov",
                duration = "4:12",
                palette = MedallionPalettes.all[2],
                isCurrent = true,
                isPlaying = true,
            )
            TrackRow(
                title = "Утро в Варзобе",
                artist = "Navo Band",
                duration = "3:05",
                palette = MedallionPalettes.all[1],
            )
        }
    }
}
