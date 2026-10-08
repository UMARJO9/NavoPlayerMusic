package tj.umar.navoplayer.core.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.theme.NavoSpacing

val NavoListHorizontalPadding = 8.dp
val NavoSummaryHorizontalPadding = NavoSpacing.ScreenHorizontal - NavoListHorizontalPadding

@Composable
fun navoListPadding(hasActivePlayback: Boolean): PaddingValues {
    val navigationBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomInset = if (hasActivePlayback) NavoSpacing.MiniPlayerListInset else NavoSpacing.ListBottomInset
    return PaddingValues(
        start = NavoListHorizontalPadding,
        top = NavoSpacing.ExtraSmall,
        end = NavoListHorizontalPadding,
        bottom = bottomInset + navigationBarBottom,
    )
}
