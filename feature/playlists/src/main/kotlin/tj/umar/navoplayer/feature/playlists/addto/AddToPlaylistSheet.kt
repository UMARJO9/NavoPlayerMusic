package tj.umar.navoplayer.feature.playlists.addto

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.animation.animatedListItem
import tj.umar.navoplayer.core.designsystem.animation.rememberListEntrance
import tj.umar.navoplayer.core.designsystem.animation.rememberMotionEnabled
import tj.umar.navoplayer.core.designsystem.component.AnimatedCheck
import tj.umar.navoplayer.core.designsystem.component.GroupLeading
import tj.umar.navoplayer.core.designsystem.component.GroupRow
import tj.umar.navoplayer.core.designsystem.icon.NavoIcons
import tj.umar.navoplayer.core.designsystem.medallion.MedallionPalettes
import tj.umar.navoplayer.core.designsystem.theme.NavoShapes
import tj.umar.navoplayer.core.designsystem.theme.NavoSpacing
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.core.domain.model.PlaylistSummary
import tj.umar.navoplayer.core.domain.model.durationMinutes
import tj.umar.navoplayer.core.ui.R as CoreUiR
import tj.umar.navoplayer.core.ui.playlist.PlaylistNameForm
import tj.umar.navoplayer.feature.playlists.R

private const val PLAYLIST_ROW_PRESSED_SCALE = 0.98f
private const val STATUS_CROSSFADE_MILLIS = 160
private const val ICON_POP_DAMPING = 0.45f
private const val ICON_POP_START_SCALE = 0.6f
private const val ICON_FADE_IN_MILLIS = 120
private const val ICON_EXIT_MILLIS = 90

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddToPlaylistSheet(
    state: AddToPlaylistState,
    sheetState: SheetState,
    onIntent: (AddToPlaylistIntent) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val entrance = rememberListEntrance()
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier,
        containerColor = NavoTheme.colors.raised,
        contentColor = NavoTheme.colors.content,
    ) {
        if (state.isNameFormVisible) {
            PlaylistNameForm(
                title = stringResource(CoreUiR.string.core_ui_new_playlist),
                confirmLabel = stringResource(CoreUiR.string.core_ui_create),
                onConfirm = { onIntent(AddToPlaylistIntent.NewPlaylistConfirmed(it)) },
                onCancel = { onIntent(AddToPlaylistIntent.NameFormDismissed) },
            )
        } else {
            AddToPlaylistContent(state = state, onIntent = onIntent, entrance = entrance)
        }
    }
}

@Composable
internal fun AddToPlaylistContent(
    state: AddToPlaylistState,
    onIntent: (AddToPlaylistIntent) -> Unit,
    entrance: () -> Float = rememberListEntrance(),
) {
    val motion = rememberMotionEnabled()
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = NavoSpacing.Large),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        item(key = "title", contentType = "title") {
            Text(
                text = stringResource(CoreUiR.string.core_ui_add_to_playlist),
                style = NavoTheme.typography.titleS,
                color = NavoTheme.colors.content,
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = NavoSpacing.Small)
                    .semantics { heading() },
            )
        }
        if (state.favoriteTrackId != null) {
            item(key = "favorite", contentType = "action") {
                val isFavorite = state.isFavorite == true
                ActionRow(
                    label = stringResource(
                        if (isFavorite) R.string.playlists_sheet_favorite_remove else R.string.playlists_sheet_favorite_add,
                    ),
                    enabled = !state.isLocked && state.isFavorite != null,
                    onClick = { onIntent(AddToPlaylistIntent.FavoriteClicked) },
                    modifier = Modifier.semantics { selected = isFavorite },
                ) {
                    FavoriteIcon(isFavorite = state.isFavorite)
                }
            }
        }
        item(key = "new", contentType = "action") {
            ActionRow(
                label = stringResource(CoreUiR.string.core_ui_new_playlist),
                enabled = !state.isLocked,
                onClick = { onIntent(AddToPlaylistIntent.NewPlaylistClicked) },
            ) {
                ActionIcon(NavoIcons.Plus)
            }
        }
        when {
            state.isLoading -> item(key = "loading", contentType = "status") { LoadingRow() }
            state.loadFailed -> item(key = "error", contentType = "status") {
                ErrorRow(onRetry = { onIntent(AddToPlaylistIntent.RetryLoad) })
            }
            else -> itemsIndexed(
                state.playlists,
                key = { _, playlist -> playlist.id },
                contentType = { _, _ -> "playlist" },
            ) { index, playlist ->
                GroupRow(
                    title = playlist.name,
                    subtitle = playlist.subtitle(),
                    leading = GroupLeading.Artwork(MedallionPalettes.forKey(playlist.id)),
                    onClick = { onIntent(AddToPlaylistIntent.PlaylistClicked(playlist.id)) },
                    modifier = Modifier.animatedListItem(this, index, motion, entrance),
                    pressedScale = PLAYLIST_ROW_PRESSED_SCALE,
                    trailing = {
                        RowStatus(
                            status = when (playlist.id) {
                                state.savingPlaylistId -> PlaylistRowStatus.Saving
                                state.addedPlaylistId -> PlaylistRowStatus.Added
                                else -> PlaylistRowStatus.Idle
                            },
                            motion = motion,
                        )
                    },
                )
            }
        }
    }
}

private enum class PlaylistRowStatus { Idle, Saving, Added }

@Composable
private fun RowStatus(status: PlaylistRowStatus, motion: Boolean) {
    Crossfade(
        targetState = status,
        animationSpec = if (motion) tween(STATUS_CROSSFADE_MILLIS) else snap(),
        label = "playlistRowStatus",
        modifier = Modifier.clearAndSetSemantics { },
    ) { shown ->
        when (shown) {
            PlaylistRowStatus.Idle -> Unit
            PlaylistRowStatus.Saving -> CircularProgressIndicator(
                color = NavoTheme.colors.accent,
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp),
            )
            PlaylistRowStatus.Added -> AnimatedCheck(visible = true)
        }
    }
}

@Composable
private fun ActionRow(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
) {
    val colors = NavoTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clip(NavoShapes.TrackRow)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(NavoShapes.Pill)
                .background(colors.accent),
            contentAlignment = Alignment.Center,
        ) {
            icon()
        }
        Text(
            text = label,
            style = NavoTheme.typography.itemTitle,
            color = colors.content,
        )
    }
}

@Composable
private fun ActionIcon(icon: ImageVector) {
    Icon(imageVector = icon, contentDescription = null, tint = NavoTheme.colors.onAccent)
}

@Composable
private fun FavoriteIcon(isFavorite: Boolean?) {
    val motion = rememberMotionEnabled()
    AnimatedContent(
        targetState = isFavorite,
        transitionSpec = {
            if (motion && initialState != null) {
                (
                    scaleIn(
                        animationSpec = spring(
                            dampingRatio = ICON_POP_DAMPING,
                            stiffness = Spring.StiffnessMediumLow,
                        ),
                        initialScale = ICON_POP_START_SCALE,
                    ) + fadeIn(tween(ICON_FADE_IN_MILLIS))
                ).togetherWith(
                    scaleOut(tween(ICON_EXIT_MILLIS), targetScale = ICON_POP_START_SCALE) +
                        fadeOut(tween(ICON_EXIT_MILLIS)),
                )
            } else {
                EnterTransition.None togetherWith ExitTransition.None
            }
        },
        contentAlignment = Alignment.Center,
        label = "favoriteIcon",
    ) { shownFavorite ->
        ActionIcon(if (shownFavorite == true) NavoIcons.HeartFilled else NavoIcons.Heart)
    }
}

@Composable
private fun LoadingRow() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(NavoSpacing.Large),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = NavoTheme.colors.accent, modifier = Modifier.size(28.dp))
    }
}

@Composable
private fun ErrorRow(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(NavoSpacing.Medium),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.playlists_sheet_error),
            style = NavoTheme.typography.body,
            color = NavoTheme.colors.contentSecondary,
        )
        TextButton(onClick = onRetry) {
            Text(
                text = stringResource(R.string.playlists_retry),
                style = NavoTheme.typography.labelStrong,
                color = NavoTheme.colors.accent,
            )
        }
    }
}

@Composable
private fun PlaylistSummary.subtitle(): String {
    val minutes = durationMinutes()
    return stringResource(
        CoreUiR.string.core_ui_group_subtitle,
        pluralStringResource(CoreUiR.plurals.core_ui_track_count, trackCount, trackCount),
        pluralStringResource(CoreUiR.plurals.core_ui_minute_count, minutes, minutes),
    )
}
