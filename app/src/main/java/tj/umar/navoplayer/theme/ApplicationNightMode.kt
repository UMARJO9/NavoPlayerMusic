package tj.umar.navoplayer.theme

import android.app.UiModeManager
import android.content.Context
import android.os.Build
import tj.umar.navoplayer.core.domain.model.ThemeMode

private var lastAppliedNightMode: Int? = null

internal fun ThemeMode.applicationNightMode(): Int = when (this) {
    ThemeMode.System -> UiModeManager.MODE_NIGHT_AUTO
    ThemeMode.Light -> UiModeManager.MODE_NIGHT_NO
    ThemeMode.Dark -> UiModeManager.MODE_NIGHT_YES
}

fun Context.syncApplicationNightMode(mode: ThemeMode) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    val target = mode.applicationNightMode()
    if (target == lastAppliedNightMode) return
    val uiModeManager = getSystemService(UiModeManager::class.java) ?: return
    uiModeManager.setApplicationNightMode(target)
    lastAppliedNightMode = target
}
