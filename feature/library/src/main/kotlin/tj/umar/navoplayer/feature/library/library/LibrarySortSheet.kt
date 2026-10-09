package tj.umar.navoplayer.feature.library.library

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.component.NavoChip
import tj.umar.navoplayer.core.designsystem.component.SettingsRadioRow
import tj.umar.navoplayer.core.designsystem.component.SettingsSectionHeader
import tj.umar.navoplayer.core.designsystem.theme.NavoSpacing
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.core.domain.model.GroupSort
import tj.umar.navoplayer.core.domain.model.GroupSortField
import tj.umar.navoplayer.core.domain.model.SortDirection
import tj.umar.navoplayer.core.domain.model.TrackSort
import tj.umar.navoplayer.core.domain.model.TrackSortField
import tj.umar.navoplayer.feature.library.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LibrarySortSheet(
    target: SortTarget,
    trackSort: TrackSort,
    groupSort: GroupSort,
    onIntent: (LibraryIntent) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = { onIntent(LibraryIntent.SortSheetDismissed) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = NavoTheme.colors.raised,
        contentColor = NavoTheme.colors.content,
    ) {
        LibrarySortContent(target = target, trackSort = trackSort, groupSort = groupSort, onIntent = onIntent)
    }
}

@Composable
internal fun LibrarySortContent(
    target: SortTarget,
    trackSort: TrackSort,
    groupSort: GroupSort,
    onIntent: (LibraryIntent) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = NavoSpacing.Large)) {
        Text(
            text = stringResource(R.string.library_sort_sheet_title),
            style = NavoTheme.typography.titleS,
            color = NavoTheme.colors.content,
            modifier = Modifier
                .padding(horizontal = NavoSpacing.ScreenHorizontal)
                .semantics { heading() },
        )
        if (target == SortTarget.Groups) {
            Text(
                text = stringResource(R.string.library_sort_groups_hint),
                style = NavoTheme.typography.secondary,
                color = NavoTheme.colors.contentSecondary,
                modifier = Modifier.padding(horizontal = NavoSpacing.ScreenHorizontal, vertical = NavoSpacing.ExtraSmall),
            )
        }
        Column(modifier = Modifier.padding(top = NavoSpacing.Small).selectableGroup()) {
            when (target) {
                SortTarget.Tracks -> TrackSortField.entries.forEach { field ->
                    SettingsRadioRow(
                        title = stringResource(field.labelRes),
                        selected = field == trackSort.field,
                        onClick = { onIntent(LibraryIntent.TrackSortFieldSelected(field)) },
                    )
                }
                SortTarget.Groups -> GroupSortField.entries.forEach { field ->
                    SettingsRadioRow(
                        title = stringResource(field.labelRes),
                        selected = field == groupSort.field,
                        onClick = { onIntent(LibraryIntent.GroupSortFieldSelected(field)) },
                    )
                }
            }
        }
        SettingsSectionHeader(title = stringResource(R.string.library_sort_order))
        val direction = if (target == SortTarget.Tracks) trackSort.direction else groupSort.direction
        Row(
            modifier = Modifier
                .padding(horizontal = NavoSpacing.ScreenHorizontal)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(NavoSpacing.Small),
        ) {
            SortDirection.entries.forEach { option ->
                NavoChip(
                    label = stringResource(option.labelRes),
                    selected = option == direction,
                    onClick = { onIntent(LibraryIntent.SortDirectionSelected(target, option)) },
                    role = Role.RadioButton,
                    unselectedContainerColor = NavoTheme.colors.high,
                )
            }
        }
    }
}

@get:StringRes
private val TrackSortField.labelRes: Int
    get() = when (this) {
        TrackSortField.Title -> R.string.library_sort_title
        TrackSortField.Artist -> R.string.library_sort_artist
        TrackSortField.Album -> R.string.library_sort_album
        TrackSortField.DateAdded -> R.string.library_sort_date_added
        TrackSortField.Duration -> R.string.library_sort_duration
    }

@get:StringRes
private val GroupSortField.labelRes: Int
    get() = when (this) {
        GroupSortField.Name -> R.string.library_sort_name
        GroupSortField.TrackCount -> R.string.library_sort_track_count
    }

@get:StringRes
private val SortDirection.labelRes: Int
    get() = when (this) {
        SortDirection.Ascending -> R.string.library_sort_ascending
        SortDirection.Descending -> R.string.library_sort_descending
    }

@Preview(widthDp = 390)
@Composable
private fun TrackSortPreview() {
    NavoTheme {
        Column(modifier = Modifier.background(NavoTheme.colors.raised).padding(top = 16.dp)) {
            LibrarySortContent(
                target = SortTarget.Tracks,
                trackSort = TrackSort(TrackSortField.DateAdded, SortDirection.Descending),
                groupSort = GroupSort.Default,
                onIntent = {},
            )
        }
    }
}

@Preview(widthDp = 390)
@Composable
private fun GroupSortPreview() {
    NavoTheme {
        Column(modifier = Modifier.background(NavoTheme.colors.raised).padding(top = 16.dp)) {
            LibrarySortContent(
                target = SortTarget.Groups,
                trackSort = TrackSort.Default,
                groupSort = GroupSort(GroupSortField.TrackCount),
                onIntent = {},
            )
        }
    }
}
