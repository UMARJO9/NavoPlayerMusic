package tj.umar.navoplayer.feature.library.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import tj.umar.navoplayer.core.designsystem.component.NavoChip
import tj.umar.navoplayer.core.designsystem.component.NavoIconButton
import tj.umar.navoplayer.core.designsystem.component.Wordmark
import tj.umar.navoplayer.core.designsystem.icon.NavoIcons
import tj.umar.navoplayer.core.designsystem.theme.NavoSpacing
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.feature.library.R

@Composable
internal fun LibraryScreen(
    state: LibraryState,
    onIntent: (LibraryIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavoTheme.colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)),
    ) {
        val tabs = LibraryTab.entries
        val pagerState = rememberPagerState(initialPage = state.selectedTab.ordinal) { tabs.size }
        val scope = rememberCoroutineScope()
        val currentOnIntent by rememberUpdatedState(onIntent)
        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.settledPage }.collect { page ->
                currentOnIntent(LibraryIntent.TabSelected(tabs[page]))
            }
        }
        LaunchedEffect(state.selectedTab) {
            val page = state.selectedTab.ordinal
            if (pagerState.settledPage != page && !pagerState.isScrollInProgress) {
                pagerState.animateScrollToPage(page)
            }
        }
        LibraryHeader(onIntent = onIntent)
        LibraryTabs(
            selectedTab = tabs[pagerState.targetPage],
            onTabClick = { tab -> scope.launch { pagerState.animateScrollToPage(tab.ordinal) } },
        )
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1,
            key = { tabs[it].name },
        ) { page ->
            val pageModifier = Modifier.fillMaxSize()
            when (tabs[page]) {
                LibraryTab.Tracks -> TracksTabContent(state, onIntent, pageModifier)
                LibraryTab.Playlists -> PlaylistsTabContent(state, onIntent, pageModifier)
                LibraryTab.Albums -> AlbumsTabContent(state, onIntent, pageModifier)
                LibraryTab.Artists -> ArtistsTabContent(state, onIntent, pageModifier)
                LibraryTab.Folders -> FoldersTabContent(state, onIntent, pageModifier)
            }
        }
    }
    state.sortSheet?.let { target ->
        LibrarySortSheet(target = target, trackSort = state.trackSort, groupSort = state.groupSort, onIntent = onIntent)
    }
}

@Composable
private fun LibraryHeader(onIntent: (LibraryIntent) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = NavoSpacing.ScreenHorizontal, top = 8.dp, end = 12.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Wordmark()
        Spacer(modifier = Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(NavoSpacing.ExtraSmall)) {
            NavoIconButton(
                icon = NavoIcons.Search,
                contentDescription = stringResource(R.string.library_search),
                onClick = { onIntent(LibraryIntent.SearchClicked) },
            )
            NavoIconButton(
                icon = NavoIcons.Settings,
                contentDescription = stringResource(R.string.library_settings),
                onClick = { onIntent(LibraryIntent.SettingsClicked) },
            )
        }
    }
}

@Composable
private fun LibraryTabs(selectedTab: LibraryTab, onTabClick: (LibraryTab) -> Unit) {
    val sectionsDescription = stringResource(R.string.library_sections)
    val listState = rememberLazyListState()
    val leadPx = with(LocalDensity.current) { NavoSpacing.ScreenHorizontal.roundToPx() }
    LaunchedEffect(selectedTab) {
        val layout = listState.layoutInfo
        val item = layout.visibleItemsInfo.firstOrNull { it.index == selectedTab.ordinal }
        val fullyVisible = item != null &&
            item.offset >= layout.viewportStartOffset &&
            item.offset + item.size <= layout.viewportEndOffset
        if (!fullyVisible) listState.animateScrollToItem(selectedTab.ordinal, -leadPx)
    }
    LazyRow(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = sectionsDescription }
            .selectableGroup(),
        contentPadding = PaddingValues(horizontal = NavoSpacing.ScreenHorizontal, vertical = NavoSpacing.Small),
        horizontalArrangement = Arrangement.spacedBy(NavoSpacing.Small),
    ) {
        items(LibraryTab.entries, key = { it.name }) { tab ->
            NavoChip(
                label = stringResource(tab.titleRes),
                selected = tab == selectedTab,
                onClick = { onTabClick(tab) },
            )
        }
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun LibraryScreenPreview() {
    NavoTheme {
        LibraryScreen(
            state = LibraryState(isLoadingTracks = false, tracks = previewTracks),
            onIntent = {},
        )
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun LibraryScreenAlbumsPreview() {
    NavoTheme {
        LibraryScreen(state = previewLibraryState.copy(selectedTab = LibraryTab.Albums), onIntent = {})
    }
}
