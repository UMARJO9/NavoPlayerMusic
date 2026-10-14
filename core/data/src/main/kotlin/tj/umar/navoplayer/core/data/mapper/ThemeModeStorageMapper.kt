package tj.umar.navoplayer.core.data.mapper

import tj.umar.navoplayer.core.domain.model.ThemeMode

internal fun ThemeMode.storageValue(): String = when (this) {
    ThemeMode.System -> "system"
    ThemeMode.Light -> "light"
    ThemeMode.Dark -> "dark"
}

internal fun themeModeOf(value: String?): ThemeMode =
    ThemeMode.entries.firstOrNull { it.storageValue() == value } ?: ThemeMode.Default
