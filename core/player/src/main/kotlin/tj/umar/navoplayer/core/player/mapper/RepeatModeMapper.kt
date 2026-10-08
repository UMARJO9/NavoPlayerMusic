package tj.umar.navoplayer.core.player.mapper

import androidx.media3.common.Player
import tj.umar.navoplayer.core.domain.model.RepeatMode

internal fun RepeatMode.toPlayerRepeatMode(): Int = when (this) {
    RepeatMode.Off -> Player.REPEAT_MODE_OFF
    RepeatMode.All -> Player.REPEAT_MODE_ALL
    RepeatMode.One -> Player.REPEAT_MODE_ONE
}

internal fun Int.toDomainRepeatMode(): RepeatMode = when (this) {
    Player.REPEAT_MODE_ALL -> RepeatMode.All
    Player.REPEAT_MODE_ONE -> RepeatMode.One
    else -> RepeatMode.Off
}
