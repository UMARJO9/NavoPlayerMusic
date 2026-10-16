package tj.umar.navoplayer.core.designsystem.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle

object NavoShapes {
    val Pill: Shape = CircleShape
    val TrackRow: Shape = RoundedCornerShape(16.dp)
    val MiniPlayer: Shape = RoundedCornerShape(20.dp)
    val Snackbar: Shape = RoundedCornerShape(16.dp)
    val GlassMiniPlayer: Shape = RoundedRectangle(20.dp)
    val GlassPill: Shape = Capsule()
}
