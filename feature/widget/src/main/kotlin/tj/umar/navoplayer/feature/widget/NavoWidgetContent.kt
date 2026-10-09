package tj.umar.navoplayer.feature.widget

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.action
import androidx.glance.action.clickable
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import tj.umar.navoplayer.core.designsystem.R as DesignR
import tj.umar.navoplayer.core.ui.R as CoreUiR

internal val TallWidgetMinHeight = 100.dp
internal val WideWidgetMinWidth = 220.dp

internal data class NavoWidgetActions(
    val open: Action,
    val togglePlayPause: Action?,
    val previous: Action?,
    val next: Action?,
)

@Composable
internal fun NavoWidgetContent(state: NavoWidgetUiState, artwork: Bitmap?, actions: NavoWidgetActions) {
    val size = LocalSize.current
    val tall = size.height >= TallWidgetMinHeight
    val wide = size.width >= WideWidgetMinWidth
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(ImageProvider(R.drawable.navo_widget_background))
            .cornerRadius(R.dimen.navo_widget_corner_radius)
            .clickable(actions.open)
            .padding(12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (tall) {
            Column(modifier = GlanceModifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Artwork(artwork = artwork, size = 64.dp)
                    Spacer(modifier = GlanceModifier.width(12.dp))
                    TrackText(state = state, titleLines = 2)
                }
                Spacer(modifier = GlanceModifier.height(10.dp))
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Controls(state = state, actions = actions, showPrevious = true, playSize = 52.dp)
                }
            }
        } else {
            Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Artwork(artwork = artwork, size = 44.dp)
                Spacer(modifier = GlanceModifier.width(12.dp))
                Box(modifier = GlanceModifier.defaultWeight()) {
                    TrackText(state = state, titleLines = 1)
                }
                Controls(state = state, actions = actions, showPrevious = wide, playSize = 44.dp)
            }
        }
    }
}

@Composable
private fun Artwork(artwork: Bitmap?, size: Dp) {
    val provider = artwork?.let(::ImageProvider) ?: ImageProvider(R.drawable.navo_widget_preview_medallion)
    Image(provider = provider, contentDescription = null, modifier = GlanceModifier.size(size))
}

@Composable
private fun TrackText(state: NavoWidgetUiState, titleLines: Int) {
    val context = LocalContext.current
    val (title, subtitle) = when (state) {
        NavoWidgetUiState.Empty ->
            context.getString(R.string.navo_widget_nothing_playing) to context.getString(R.string.navo_widget_tap_to_open)
        is NavoWidgetUiState.Playing ->
            (state.title ?: context.getString(CoreUiR.string.core_ui_unknown_title)) to
                (state.artist ?: context.getString(CoreUiR.string.core_ui_unknown_artist))
    }
    Column {
        Text(
            text = title,
            maxLines = titleLines,
            style = TextStyle(
                color = ColorProvider(R.color.navo_widget_content),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
        Text(
            text = subtitle,
            maxLines = 1,
            style = TextStyle(color = ColorProvider(R.color.navo_widget_content_secondary), fontSize = 12.sp),
        )
    }
}

@Composable
private fun Controls(state: NavoWidgetUiState, actions: NavoWidgetActions, showPrevious: Boolean, playSize: Dp) {
    val context = LocalContext.current
    val isPlaying = state is NavoWidgetUiState.Playing && state.isPlaying
    if (showPrevious) {
        TransportButton(
            icon = DesignR.drawable.navo_ic_skip_previous,
            description = context.getString(R.string.navo_widget_previous),
            action = actions.previous,
        )
    }
    Box(
        modifier = GlanceModifier
            .size(playSize)
            .background(ImageProvider(R.drawable.navo_widget_play_background))
            .clickable(actions.togglePlayPause ?: actions.open),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            provider = ImageProvider(if (isPlaying) DesignR.drawable.navo_ic_pause else DesignR.drawable.navo_ic_play),
            contentDescription = context.getString(if (isPlaying) R.string.navo_widget_pause else R.string.navo_widget_play),
            colorFilter = ColorFilter.tint(ColorProvider(R.color.navo_widget_on_accent)),
            modifier = GlanceModifier.size(playSize / 2),
        )
    }
    TransportButton(
        icon = DesignR.drawable.navo_ic_skip_next,
        description = context.getString(R.string.navo_widget_next),
        action = actions.next,
    )
}

@Composable
private fun TransportButton(icon: Int, description: String, action: Action?) {
    val enabled = action != null
    val modifier = GlanceModifier.size(40.dp).padding(10.dp)
    Image(
        provider = ImageProvider(icon),
        contentDescription = description,
        colorFilter = ColorFilter.tint(
            ColorProvider(if (enabled) R.color.navo_widget_content else R.color.navo_widget_disabled),
        ),
        modifier = if (action != null) modifier.clickable(action) else modifier,
    )
}

private val previewPlaying = NavoWidgetUiState.Playing(
    trackId = 1,
    title = "Утро в Варзобе",
    artist = "Navo Band",
    isPlaying = true,
)

@Composable
private fun previewActions(enabled: Boolean) = NavoWidgetActions(
    open = action {},
    togglePlayPause = action {},
    previous = if (enabled) action {} else null,
    next = if (enabled) action {} else null,
)

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 180, heightDp = 64)
@Composable
private fun NavoWidgetNarrowPreview() {
    NavoWidgetContent(state = previewPlaying, artwork = null, actions = previewActions(enabled = true))
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 280, heightDp = 64)
@Composable
private fun NavoWidgetWidePreview() {
    NavoWidgetContent(state = previewPlaying, artwork = null, actions = previewActions(enabled = true))
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 280, heightDp = 130)
@Composable
private fun NavoWidgetTallPreview() {
    NavoWidgetContent(state = previewPlaying.copy(isPlaying = false), artwork = null, actions = previewActions(enabled = true))
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 280, heightDp = 64)
@Composable
private fun NavoWidgetEmptyPreview() {
    NavoWidgetContent(state = NavoWidgetUiState.Empty, artwork = null, actions = previewActions(enabled = false))
}
