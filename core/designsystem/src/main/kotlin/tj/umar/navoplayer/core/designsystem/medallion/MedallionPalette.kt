package tj.umar.navoplayer.core.designsystem.medallion

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import tj.umar.navoplayer.core.designsystem.theme.Ivory
import kotlin.math.abs

@Immutable
data class MedallionPalette(
    val background: Color,
    val petals: Color,
    val center: Color,
)

object MedallionPalettes {

    val all: List<MedallionPalette> = listOf(
        MedallionPalette(Color(0xFFC8414B), Color(0xFFF2C14E), Color(0xFF13204A)),
        MedallionPalette(Color(0xFF2E7D74), Color(0xFFF3EEDF), Color(0xFFC8414B)),
        MedallionPalette(Color(0xFFE3B04B), Color(0xFF13204A), Color(0xFFC8414B)),
        MedallionPalette(Color(0xFF6B3FA0), Color(0xFFF2C14E), Color(0xFF2E7D74)),
        MedallionPalette(Color(0xFFF3EEDF), Color(0xFF2E7D74), Color(0xFFC8414B)),
        MedallionPalette(Color(0xFF1F5FA8), Color(0xFFF3EEDF), Color(0xFFE3B04B)),
        MedallionPalette(Color(0xFF8A3B2E), Color(0xFFE3B04B), Color(0xFFF3EEDF)),
    )

    fun forKey(key: Long): MedallionPalette = all[abs(key.hashCode() % all.size)]
}

internal fun ivoryOn(surface: Color, fallback: Color): Color = if (surface == Ivory) fallback else Ivory
