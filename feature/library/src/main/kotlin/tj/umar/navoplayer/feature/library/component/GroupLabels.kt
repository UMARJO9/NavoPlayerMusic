package tj.umar.navoplayer.feature.library.component

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
import tj.umar.navoplayer.core.ui.R as CoreUiR
import tj.umar.navoplayer.feature.library.R

@Composable
internal fun TrackGroup.displayTitle(): String = when (this) {
    is Album -> title ?: stringResource(CoreUiR.string.core_ui_unknown_album)
    is Artist -> name ?: stringResource(CoreUiR.string.core_ui_unknown_artist)
    is Folder -> name ?: stringResource(CoreUiR.string.core_ui_unknown_folder)
}

@Composable
internal fun TrackGroup.description(): String? = when (this) {
    is Album -> when {
        hasVariousArtists -> stringResource(CoreUiR.string.core_ui_various_artists)
        else -> artist ?: stringResource(CoreUiR.string.core_ui_unknown_artist)
    }
    is Artist -> pluralStringResource(R.plurals.library_album_count, albumCount, albumCount)
    is Folder -> path
}

@Composable
internal fun TrackGroup.displaySubtitle(): String {
    val trackCount = pluralStringResource(R.plurals.library_track_count, tracks.size, tracks.size)
    val description = description() ?: return trackCount
    return stringResource(R.string.library_group_subtitle, description, trackCount)
}

internal fun TrackGroupKey.leading(): GroupLeading = when (type) {
    TrackGroupType.Folder -> GroupLeading.Folder
    else -> GroupLeading.Artwork(MedallionPalettes.forKey(id ?: name?.hashCode()?.toLong() ?: type.ordinal.toLong()))
}

internal fun TrackGroupKey.stableKey(): String = "${type.name}:${id ?: ""}:${name.orEmpty()}"
