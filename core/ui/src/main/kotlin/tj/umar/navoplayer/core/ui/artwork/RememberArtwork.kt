package tj.umar.navoplayer.core.ui.artwork

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import tj.umar.navoplayer.core.domain.model.ArtworkKey
import tj.umar.navoplayer.core.domain.model.TrackGroup
import tj.umar.navoplayer.core.domain.model.artworkKey

@Composable
fun rememberArtwork(key: ArtworkKey?, size: Dp): State<ImageBitmap?> {
    val loader = LocalArtworkLoader.current
    val sizePx = with(LocalDensity.current) { size.roundToPx() }.coerceAtLeast(1)
    val state = remember(key, sizePx, loader) {
        mutableStateOf(key?.let { loader.cached(it, sizePx)?.asImageBitmap() })
    }
    LaunchedEffect(key, sizePx, loader) {
        if (state.value == null && key != null) {
            state.value = loader.load(key, sizePx)?.asImageBitmap()
        }
    }
    return state
}

@Composable
fun rememberGroupArtwork(group: TrackGroup, size: Dp): State<ImageBitmap?> {
    val key = remember(group) { group.artworkKey() }
    return rememberArtwork(key, size)
}
