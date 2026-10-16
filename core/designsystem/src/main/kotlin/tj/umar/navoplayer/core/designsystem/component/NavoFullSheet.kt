package tj.umar.navoplayer.core.designsystem.component

import android.os.Build
import android.view.View
import android.view.Window

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme

private val FullSheetCorner = 32.dp
private val FullSheetShape = RoundedCornerShape(topStart = FullSheetCorner, topEnd = FullSheetCorner)
private val HandleWidth = 40.dp
private val HandleHeight = 4.dp
private val HandleTopPadding = 10.dp
private val HandleBottomPadding = 6.dp
private val HandleAreaHeight = HandleTopPadding + HandleHeight + HandleBottomPadding
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
    content: @Composable BoxScope.(contentPadding: PaddingValues) -> Unit,
) {
    val colors = NavoTheme.colors
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(top = topGap),
        sheetState = sheetState,
        shape = FullSheetShape,
        containerColor = containerColor,
        contentColor = colors.content,
        scrimColor = colors.shadow.copy(alpha = SCRIM_ALPHA),
        dragHandle = null,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        properties = ModalBottomSheetProperties(
            isAppearanceLightStatusBars = false,
            isAppearanceLightNavigationBars = !colors.isDark,
        ),
    ) {
        val view = LocalView.current
        LaunchedEffect(view) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                view.dialogWindow()?.isNavigationBarContrastEnforced = false
            }
        }
        val contentPadding = WindowInsets.safeDrawing
            .only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
            .add(WindowInsets(top = HandleAreaHeight))
            .asPaddingValues()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(FullSheetShape),
        ) {
            content(contentPadding)
            NavoSheetHandle(modifier = Modifier.align(Alignment.TopCenter))
        }
    }
}

@Composable
private fun NavoSheetHandle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = HandleTopPadding, bottom = HandleBottomPadding),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = HandleWidth, height = HandleHeight)
                .background(NavoTheme.colors.contentSecondary.copy(alpha = HANDLE_ALPHA), RoundedCornerShape(50)),
        )
    }
}

private fun View.dialogWindow(): Window? =
    generateSequence(parent) { it.parent }
        .filterIsInstance<DialogWindowProvider>()
        .firstOrNull()
        ?.window
