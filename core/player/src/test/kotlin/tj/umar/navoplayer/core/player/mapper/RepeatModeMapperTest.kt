package tj.umar.navoplayer.core.player.mapper

import androidx.media3.common.Player
import org.junit.Assert.assertEquals
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.RepeatMode

class RepeatModeMapperTest {

    @Test
    fun `domain modes map to player modes and back`() {
        RepeatMode.entries.forEach { mode ->
            assertEquals(mode, mode.toPlayerRepeatMode().toDomainRepeatMode())
        }
        assertEquals(Player.REPEAT_MODE_ONE, RepeatMode.One.toPlayerRepeatMode())
    }

    @Test
    fun `unknown player mode is off`() {
        assertEquals(RepeatMode.Off, 42.toDomainRepeatMode())
    }
}
