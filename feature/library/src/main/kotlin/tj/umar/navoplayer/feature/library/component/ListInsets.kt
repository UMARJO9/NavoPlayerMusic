package tj.umar.navoplayer.feature.library.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.theme.NavoSpacing

internal val ListHorizontalPadding = 8.dp
internal val SummaryHorizontalPadding = NavoSpacing.ScreenHorizontal - ListHorizontalPadding

@Composable
internal fun libraryListPadding(hasActivePlayback: Boolean): PaddingValues {
    val navigationBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomInset = if (hasActivePlayback) NavoSpacing.MiniPlayerListInset else NavoSpacing.ListBottomInset
    return PaddingValues(
        start = ListHorizontalPadding,
        top = NavoSpacing.ExtraSmall,
        end = ListHorizontalPadding,
        bottom = bottomInset + navigationBarBottom,
    )
}
