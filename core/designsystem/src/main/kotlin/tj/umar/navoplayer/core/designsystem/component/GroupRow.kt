package tj.umar.navoplayer.core.designsystem.component

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.icon.NavoIcons
import tj.umar.navoplayer.core.designsystem.medallion.MedallionArtwork
import tj.umar.navoplayer.core.designsystem.medallion.MedallionPalette
import tj.umar.navoplayer.core.designsystem.modifier.pressScale
import tj.umar.navoplayer.core.designsystem.theme.NavoShapes
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.core.designsystem.R

sealed interface GroupLeading {
    data class Artwork(val palette: MedallionPalette) : GroupLeading
    data object Folder : GroupLeading
    data object Favorites : GroupLeading
}

@Composable
fun GroupRow(
    title: String,
    subtitle: String,
    leading: GroupLeading,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    pressedScale: Float = 1f,
    trailing: (@Composable () -> Unit)? = null,
    artwork: ImageBitmap? = null,
) {
    val typography = NavoTheme.typography
    val colors = NavoTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val pressModifier = if (pressedScale < 1f) Modifier.pressScale(interactionSource, pressedScale) else Modifier
    Row(
        modifier = modifier
            .then(pressModifier)
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .semantics(mergeDescendants = true) { }
            .clip(NavoShapes.TrackRow)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClickLabel = stringResource(R.string.designsystem_open),
                onClick = onClick,
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        GroupArtwork(leading = leading, size = 48.dp, artwork = artwork)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = title,
                style = typography.itemTitle,
                color = colors.content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = typography.secondary,
                color = colors.contentSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (trailing != null) {
            Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                trailing()
            }
        }
    }
}

@Composable
fun GroupArtwork(
    leading: GroupLeading,
    size: Dp,
    modifier: Modifier = Modifier,
    artwork: ImageBitmap? = null,
) {
    when (leading) {
        is GroupLeading.Artwork -> MedallionArtwork(
            palette = leading.palette,
            image = artwork,
            modifier = modifier.size(size),
        )
        GroupLeading.Folder -> Box(
            modifier = modifier
                .size(size)
                .background(NavoTheme.colors.raised, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = NavoIcons.Folder,
                contentDescription = null,
                tint = NavoTheme.colors.accent,
                modifier = Modifier.size(size / 2),
            )
        }
        GroupLeading.Favorites -> Box(
            modifier = modifier
                .size(size)
                .background(NavoTheme.colors.accent, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = NavoIcons.HeartFilled,
                contentDescription = null,
                tint = NavoTheme.colors.onAccent,
                modifier = Modifier.size(size / 2),
            )
        }
    }
}

@Preview
@Composable
private fun FavoritesGroupRowPreview() {
    NavoTheme {
        Column(modifier = Modifier.background(NavoTheme.colors.background).padding(8.dp)) {
            GroupRow(
                title = "Избранное",
                subtitle = "12 треков · 48 минут",
                leading = GroupLeading.Favorites,
                onClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun TrailingGroupRowPreview() {
    NavoTheme {
        Column(modifier = Modifier.background(NavoTheme.colors.background).padding(8.dp)) {
            GroupRow(
                title = "Вечерний плейлист",
                subtitle = "8 треков · 31 минута",
                leading = GroupLeading.Folder,
                onClick = {},
                pressedScale = 0.98f,
                trailing = {
                    Icon(
                        imageVector = NavoIcons.Check,
                        contentDescription = null,
                        tint = NavoTheme.colors.accent,
                    )
                },
            )
        }
    }
}
