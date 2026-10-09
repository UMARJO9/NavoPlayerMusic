package tj.umar.navoplayer.core.data.mapper

import tj.umar.navoplayer.core.domain.model.GroupSortField
import tj.umar.navoplayer.core.domain.model.SortDirection
import tj.umar.navoplayer.core.domain.model.TrackSortField

internal fun TrackSortField.storageValue(): String = when (this) {
    TrackSortField.Title -> "title"
    TrackSortField.Artist -> "artist"
    TrackSortField.Album -> "album"
    TrackSortField.DateAdded -> "date_added"
    TrackSortField.Duration -> "duration"
}

internal fun trackSortFieldOf(value: String?): TrackSortField =
    TrackSortField.entries.firstOrNull { it.storageValue() == value } ?: TrackSortField.Title

internal fun GroupSortField.storageValue(): String = when (this) {
    GroupSortField.Name -> "name"
    GroupSortField.TrackCount -> "track_count"
}

internal fun groupSortFieldOf(value: String?): GroupSortField =
    GroupSortField.entries.firstOrNull { it.storageValue() == value } ?: GroupSortField.Name

internal fun directionOf(descending: Boolean): SortDirection =
    if (descending) SortDirection.Descending else SortDirection.Ascending
