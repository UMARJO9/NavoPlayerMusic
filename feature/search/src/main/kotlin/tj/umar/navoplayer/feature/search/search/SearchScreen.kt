package tj.umar.navoplayer.feature.search.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import kotlinx.coroutines.flow.filter
import tj.umar.navoplayer.core.designsystem.component.ContentPhase
import tj.umar.navoplayer.core.designsystem.component.GroupRow
import tj.umar.navoplayer.core.designsystem.component.NavoIconButton
import tj.umar.navoplayer.core.designsystem.component.NavoSearchField
import tj.umar.navoplayer.core.designsystem.component.NavoSummaryHorizontalPadding
import tj.umar.navoplayer.core.designsystem.component.PhasedContent
import tj.umar.navoplayer.core.designsystem.component.StateMessage
import tj.umar.navoplayer.core.designsystem.component.navoListPadding
import tj.umar.navoplayer.core.designsystem.icon.NavoIcons
import tj.umar.navoplayer.core.designsystem.medallion.MedallionPalettes
import tj.umar.navoplayer.core.designsystem.theme.NavoSpacing
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.core.domain.model.TrackGroup
import tj.umar.navoplayer.core.ui.group.displaySubtitle
import tj.umar.navoplayer.core.ui.group.displayTitle
import tj.umar.navoplayer.core.ui.group.leading
import tj.umar.navoplayer.core.ui.group.stableKey
import tj.umar.navoplayer.core.ui.track.TrackListItem
import tj.umar.navoplayer.feature.search.R

@Composable
internal fun SearchScreen(
    state: SearchState,
    onIntent: (SearchIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    var hasAutoFocused by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!hasAutoFocused) {
            hasAutoFocused = true
            focusRequester.requestFocus()
            keyboard?.show()
        }
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavoTheme.colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)),
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = NavoSpacing.ScreenHorizontal, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavoIconButton(
                icon = NavoIcons.ChevronLeft,
                contentDescription = stringResource(R.string.search_back),
                onClick = { onIntent(SearchIntent.BackClicked) },
            )
            NavoSearchField(
                value = state.query,
                onValueChange = { onIntent(SearchIntent.QueryChanged(it)) },
                placeholder = stringResource(R.string.search_field_hint),
                clearContentDescription = stringResource(R.string.search_clear),
                onClear = { onIntent(SearchIntent.ClearQueryClicked) },
                focusRequester = focusRequester,
                onSearch = { keyboard?.hide() },
                modifier = Modifier.weight(1f),
            )
        }
        PhasedContent(
            phase = state.contentPhase(),
            modifier = Modifier.fillMaxSize(),
            empty = {
                if (state.phase == SearchPhase.NoResults) {
                    StateMessage(
                        message = stringResource(R.string.search_no_results, state.resultsQuery),
                        palette = MedallionPalettes.all[5],
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    )
                } else {
                    StateMessage(message = stringResource(R.string.search_idle_hint), palette = MedallionPalettes.all[2])
                }
            },
            error = {
                StateMessage(
                    message = stringResource(R.string.search_error),
                    palette = MedallionPalettes.all[0],
                    actionLabel = stringResource(R.string.search_retry),
                    onAction = { onIntent(SearchIntent.RetryClicked) },
                )
            },
        ) {
            SearchResultsList(state = state, onIntent = onIntent, onScroll = { keyboard?.hide() })
        }
    }
}

private fun SearchState.contentPhase(): ContentPhase = when (phase) {
    SearchPhase.Idle, SearchPhase.NoResults -> ContentPhase.Empty
    SearchPhase.Loading -> ContentPhase.Loading
    SearchPhase.Results -> ContentPhase.Content
    SearchPhase.Error -> ContentPhase.Error
}

@Composable
private fun SearchResultsList(state: SearchState, onIntent: (SearchIntent) -> Unit, onScroll: () -> Unit) {
    val listState = rememberLazyListState()
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }.filter { it }.collect { onScroll() }
    }
    val listPadding = navoListPadding(state.hasActivePlayback)
    val imeBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = listPadding.calculateLeftPadding(LayoutDirection.Ltr),
            top = listPadding.calculateTopPadding(),
            end = listPadding.calculateRightPadding(LayoutDirection.Ltr),
            bottom = max(listPadding.calculateBottomPadding(), imeBottom),
        ),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        groupSection("artists", R.string.search_section_artists, state.artists, state.artistCount, onIntent)
        groupSection("albums", R.string.search_section_albums, state.albums, state.albumCount, onIntent)
        if (state.tracks.isNotEmpty()) {
            item(key = "header:tracks", contentType = "header") {
                SectionHeader(title = stringResource(R.string.search_section_tracks), count = state.tracks.size)
            }
            items(state.tracks, key = { "t:${it.id}" }, contentType = { "track" }) { track ->
                TrackListItem(
                    track = track,
                    isCurrent = track.id == state.currentTrackId,
                    isPlaying = track.id == state.currentTrackId && state.isPlaying,
                    onClick = { onIntent(SearchIntent.TrackClicked(track.id)) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

private fun LazyListScope.groupSection(
    name: String,
    titleRes: Int,
    groups: List<TrackGroup>,
    totalCount: Int,
    onIntent: (SearchIntent) -> Unit,
) {
    if (groups.isEmpty()) return
    item(key = "header:$name", contentType = "header") {
        SectionHeader(title = stringResource(titleRes), count = maxOf(totalCount, groups.size))
    }
    items(groups, key = { "$name:${it.key.stableKey()}" }, contentType = { "group" }) { group ->
        GroupRow(
            title = group.displayTitle(),
            subtitle = group.displaySubtitle(),
            leading = group.key.leading(),
            onClick = { onIntent(SearchIntent.GroupClicked(group.key)) },
            modifier = Modifier.animateItem(),
        )
    }
}

@Composable
private fun SectionHeader(title: String, count: Int) {
    Text(
        text = stringResource(R.string.search_section_count, title, count),
        style = NavoTheme.typography.titleS,
        color = NavoTheme.colors.content,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = NavoSummaryHorizontalPadding, end = NavoSummaryHorizontalPadding, top = 16.dp, bottom = 6.dp)
            .semantics { heading() },
    )
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun SearchIdlePreview() {
    NavoTheme {
        SearchScreen(state = SearchState(), onIntent = {})
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun SearchResultsPreview() {
    NavoTheme {
        SearchScreen(state = previewSearchState, onIntent = {})
    }
}

@Preview(widthDp = 390, heightDp = 600)
@Composable
private fun SearchNoResultsPreview() {
    NavoTheme {
        SearchScreen(
            state = SearchState(query = "zzz", phase = SearchPhase.NoResults, resultsQuery = "zzz"),
            onIntent = {},
        )
    }
}
