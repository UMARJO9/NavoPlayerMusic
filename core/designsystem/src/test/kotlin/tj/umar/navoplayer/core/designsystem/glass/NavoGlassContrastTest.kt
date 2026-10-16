package tj.umar.navoplayer.core.designsystem.glass

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertTrue
import org.junit.Test
import tj.umar.navoplayer.core.designsystem.theme.LapisNavoColors
import tj.umar.navoplayer.core.designsystem.theme.NavoColors
import tj.umar.navoplayer.core.designsystem.theme.ParchmentNavoColors
import kotlin.math.max
import kotlin.math.min

private const val BACKDROP_MIX = 0.35f
private const val AA = 4.5f

class NavoGlassContrastTest {

    private val themes = listOf("dark" to LapisNavoColors, "light" to ParchmentNavoColors)

    private fun contrast(foreground: Color, background: Color): Float {
        val solid = foreground.compositeOver(background)
        val first = solid.luminance()
        val second = background.luminance()
        return (max(first, second) + 0.05f) / (min(first, second) + 0.05f)
    }

    private fun assertReadable(name: String, foreground: Color, background: Color) {
        val ratio = contrast(foreground, background)
        assertTrue("$name contrast $ratio is below $AA", ratio >= AA)
    }

    private fun styles(colors: NavoColors) = listOf("bar" to barGlassStyle(colors), "pill" to pillGlassStyle(colors))

    @Test
    fun `content stays readable on glass over mixed backdrops`() {
        themes.forEach { (themeName, colors) ->
            styles(colors).forEach { (styleName, style) ->
                listOf("white" to Color.White, "black" to Color.Black).forEach { (mixName, mix) ->
                    val backdrop = lerp(colors.background, mix, BACKDROP_MIX)
                    val surface = style.tint.compositeOver(backdrop)
                    val label = "$themeName $styleName over $mixName mix"
                    assertReadable("content $label", colors.content, surface)
                    assertReadable("secondaryContent $label", style.secondaryContent, surface)
                }
            }
        }
    }

    @Test
    fun `fallback tint stays readable over white and black`() {
        themes.forEach { (themeName, colors) ->
            styles(colors).forEach { (styleName, style) ->
                listOf("white" to Color.White, "black" to Color.Black).forEach { (backdropName, backdrop) ->
                    val surface = style.fallbackTint.compositeOver(backdrop)
                    val label = "$themeName $styleName fallback over $backdropName"
                    assertReadable("content $label", colors.content, surface)
                    assertReadable("secondaryContent $label", style.secondaryContent, surface)
                }
            }
        }
    }
}
