package tj.umar.navoplayer.core.designsystem.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.theme.NavoShapes
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme

@Composable
fun NavoGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = NavoShapes.GlassPill,
    style: NavoGlassStyle = NavoGlassDefaults.pill(),
    backdrop: NavoGlassBackdrop? = LocalNavoGlassBackdrop.current,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .navoGlass(backdrop, shape, style)
            .clip(shape),
        contentAlignment = contentAlignment,
        content = content,
    )
}

@Composable
private fun GlassPreviewFrame(darkTheme: Boolean) {
    NavoTheme(darkTheme = darkTheme) {
        val colors = NavoTheme.colors
        val backdrop = rememberNavoGlassBackdrop()
        Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .navoGlassSource(backdrop)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFE3B04B), Color(0xFFC8414B), Color(0xFF2E7D74), Color(0xFF13204A)),
                        ),
                    ),
            )
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                NavoGlassSurface(
                    modifier = Modifier.fillMaxWidth().height(68.dp),
                    shape = NavoShapes.GlassMiniPlayer,
                    style = NavoGlassDefaults.bar(),
                    backdrop = backdrop,
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Text("Ҷавонӣ", color = colors.content, style = NavoTheme.typography.label)
                        Text("Daler Nazarov", color = NavoGlassDefaults.bar().secondaryContent, style = NavoTheme.typography.caption)
                    }
                }
                Spacer(Modifier.height(24.dp))
                Row {
                    NavoGlassSurface(modifier = Modifier.size(48.dp), backdrop = backdrop) {}
                    Spacer(Modifier.width(16.dp))
                    NavoGlassSurface(modifier = Modifier.width(160.dp).height(48.dp), backdrop = backdrop) {
                        Text("12:40", color = colors.content, style = NavoTheme.typography.label)
                    }
                }
            }
        }
    }
}

@Preview(widthDp = 360, heightDp = 220)
@Composable
private fun NavoGlassSurfaceDarkPreview() {
    GlassPreviewFrame(darkTheme = true)
}

@Preview(widthDp = 360, heightDp = 220)
@Composable
private fun NavoGlassSurfaceLightPreview() {
    GlassPreviewFrame(darkTheme = false)
}
