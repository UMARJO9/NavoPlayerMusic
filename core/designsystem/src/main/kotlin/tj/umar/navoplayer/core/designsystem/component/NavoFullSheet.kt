package tj.umar.navoplayer.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme

private val FullSheetCorner = 32.dp
private val HandleWidth = 40.dp
private val HandleHeight = 4.dp
private const val HANDLE_ALPHA = 0.45f
private const val SCRIM_ALPHA = 0.6f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavoFullSheet(
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    topGap: Dp = 8.dp,
    containerColor: Color = NavoTheme.colors.background,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = NavoTheme.colors
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(top = topGap),
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = FullSheetCorner, topEnd = FullSheetCorner),
        containerColor = containerColor,
        contentColor = colors.content,
        scrimColor = colors.shadow.copy(alpha = SCRIM_ALPHA),
        dragHandle = { NavoSheetHandle() },
        contentWindowInsets = { WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal) },
        properties = ModalBottomSheetProperties(
            isAppearanceLightStatusBars = false,
            isAppearanceLightNavigationBars = !colors.isDark,
        ),
        content = content,
    )
}

@Composable
private fun NavoSheetHandle() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = HandleWidth, height = HandleHeight)
                .background(NavoTheme.colors.contentSecondary.copy(alpha = HANDLE_ALPHA), RoundedCornerShape(50)),
        )
    }
}
