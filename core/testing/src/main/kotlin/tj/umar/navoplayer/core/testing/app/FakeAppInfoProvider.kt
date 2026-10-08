package tj.umar.navoplayer.core.testing.app

import tj.umar.navoplayer.core.domain.app.AppInfoProvider
import tj.umar.navoplayer.core.domain.model.AppInfo

class FakeAppInfoProvider(private val versionName: String = "1.0") : AppInfoProvider {
    override fun appInfo(): AppInfo = AppInfo(versionName)
}
