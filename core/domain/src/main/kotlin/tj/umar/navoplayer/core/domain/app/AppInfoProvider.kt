package tj.umar.navoplayer.core.domain.app

import tj.umar.navoplayer.core.domain.model.AppInfo

fun interface AppInfoProvider {
    fun appInfo(): AppInfo
}
