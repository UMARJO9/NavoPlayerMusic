package tj.umar.navoplayer.core.ui.group

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import tj.umar.navoplayer.core.designsystem.component.GroupLeading
import tj.umar.navoplayer.core.designsystem.medallion.MedallionPalettes
import tj.umar.navoplayer.core.domain.model.Album
import tj.umar.navoplayer.core.domain.model.Artist
import tj.umar.navoplayer.core.domain.model.Folder
import tj.umar.navoplayer.core.domain.model.TrackGroup
import tj.umar.navoplayer.core.domain.model.TrackGroupKey
import tj.umar.navoplayer.core.domain.model.TrackGroupType
import tj.umar.navoplayer.core.ui.R

@Composable
fun TrackGroup.displayTitle(): String = when (this) {
    is Album -> title ?: stringResource(R.string.core_ui_unknown_album)
    is Artist -> name ?: stringResource(R.string.core_ui_unknown_artist)
    is Folder -> name ?: stringResource(R.string.core_ui_unknown_folder)
}

@Composable
fun TrackGroup.description(): String? = when (this) {
    is Album -> when {
        hasVariousArtists -> stringResource(R.string.core_ui_various_artists)
        else -> artist ?: stringResource(R.string.core_ui_unknown_artist)
    }
    is Artist -> pluralStringResource(R.plurals.core_ui_album_count, albumCount, albumCount)
    is Folder -> path
}

@Composable
fun TrackGroup.displaySubtitle(): String {
    val trackCount = pluralStringResource(R.plurals.core_ui_track_count, tracks.size, tracks.size)
    val description = description() ?: return trackCount
    return stringResource(R.string.core_ui_group_subtitle, description, trackCount)
}

fun TrackGroupKey.leading(): GroupLeading = when (type) {
    TrackGroupType.Folder -> GroupLeading.Folder
    else -> GroupLeading.Artwork(MedallionPalettes.forKey(id ?: name?.hashCode()?.toLong() ?: type.ordinal.toLong()))
}

fun TrackGroupKey.stableKey(): String = "${type.name}:${id ?: ""}:${name.orEmpty()}"
