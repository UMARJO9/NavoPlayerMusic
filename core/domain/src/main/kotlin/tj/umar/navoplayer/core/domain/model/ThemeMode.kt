package tj.umar.navoplayer.core.domain.model

enum class ThemeMode {
    System,
    Light,
    Dark,
    ;

    companion object {
        val Default: ThemeMode = Dark
    }
}

fun ThemeMode.resolveDarkTheme(systemInDarkTheme: Boolean): Boolean = when (this) {
    ThemeMode.System -> systemInDarkTheme
    ThemeMode.Light -> false
    ThemeMode.Dark -> true
}
