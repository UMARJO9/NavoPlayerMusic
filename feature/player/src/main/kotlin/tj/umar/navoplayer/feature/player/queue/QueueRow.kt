package tj.umar.navoplayer.feature.player.queue

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.component.NavoIconButton
import tj.umar.navoplayer.core.designsystem.equalizer.EqualizerBars
import tj.umar.navoplayer.core.designsystem.icon.NavoIcons
import tj.umar.navoplayer.core.designsystem.medallion.Medallion
import tj.umar.navoplayer.core.designsystem.medallion.MedallionPalettes
import tj.umar.navoplayer.core.designsystem.theme.NavoShapes
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.core.domain.model.Track
import tj.umar.navoplayer.feature.player.R
import tj.umar.navoplayer.core.ui.R as CoreUiR

@Composable
internal fun QueueRow(
    track: Track,
    isCurrent: Boolean,
    isPlaying: Boolean,
    isDragging: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    handleModifier: Modifier,
    modifier: Modifier = Modifier,
) {
    val colors = NavoTheme.colors
    val typography = NavoTheme.typography
    val background by animateColorAsState(
        targetValue = when {
            isDragging -> colors.high
            isCurrent -> colors.raised
            else -> Color.Transparent
        },
        label = "queueRowBackground",
    )
    val palette = remember(track.id) { MedallionPalettes.forKey(track.id) }
    val playLabel = stringResource(R.string.player_queue_play_item)
    val removeLabel = stringResource(R.string.player_queue_remove)
    val moveUpLabel = stringResource(R.string.player_queue_move_up)
    val moveDownLabel = stringResource(R.string.player_queue_move_down)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .semantics(mergeDescendants = true) {
                customActions = buildList {
                    if (canMoveUp) add(CustomAccessibilityAction(moveUpLabel) { onMoveUp(); true })
                    if (canMoveDown) add(CustomAccessibilityAction(moveDownLabel) { onMoveDown(); true })
                    if (!isCurrent) add(CustomAccessibilityAction(removeLabel) { onRemove(); true })
                }
            }
            .clip(NavoShapes.TrackRow)
            .background(background)
            .clickable(onClickLabel = playLabel, role = Role.Button, onClick = onClick)
            .padding(start = 4.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = handleModifier
                .size(40.dp)
                .clearAndSetSemantics { },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = NavoIcons.DragHandle,
                contentDescription = null,
                tint = colors.contentSecondary,
                modifier = Modifier.size(22.dp),
            )
        }
        Medallion(palette = palette, modifier = Modifier.size(44.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = track.title,
                style = typography.itemTitle,
                color = if (isCurrent) colors.accent else colors.content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = track.artist ?: stringResource(CoreUiR.string.core_ui_unknown_artist),
                style = typography.secondary,
                color = colors.contentSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
            if (isCurrent) {
                EqualizerBars(paused = !isPlaying)
            } else {
                NavoIconButton(
                    icon = NavoIcons.Close,
                    contentDescription = removeLabel,
                    onClick = onRemove,
                    iconSize = 20.dp,
                    tint = colors.contentSecondary,
                )
            }
        }
    }
}

@Preview(widthDp = 390)
@Composable
private fun QueueRowPreview() {
    NavoTheme {
        Column(modifier = Modifier.background(NavoTheme.colors.background).padding(horizontal = 8.dp)) {
            previewQueueState.items.take(3).forEachIndexed { index, item ->
                QueueRow(
                    track = item.track,
                    isCurrent = index == 1,
                    isPlaying = true,
                    isDragging = false,
                    canMoveUp = index > 0,
                    canMoveDown = index < 2,
                    onClick = {},
                    onRemove = {},
                    onMoveUp = {},
                    onMoveDown = {},
                    handleModifier = Modifier,
                )
            }
        }
    }
}
