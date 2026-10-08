package tj.umar.navoplayer.core.domain.model

enum class SortDirection { Ascending, Descending }

enum class TrackSortField { Title, Artist, Album, DateAdded, Duration }

data class TrackSort(
    val field: TrackSortField = TrackSortField.Title,
    val direction: SortDirection = SortDirection.Ascending,
) {
    companion object {
        val Default = TrackSort()
    }
}

enum class GroupSortField { Name, TrackCount }

data class GroupSort(
    val field: GroupSortField = GroupSortField.Name,
    val direction: SortDirection = SortDirection.Ascending,
) {
    companion object {
        val Default = GroupSort()
    }
}
