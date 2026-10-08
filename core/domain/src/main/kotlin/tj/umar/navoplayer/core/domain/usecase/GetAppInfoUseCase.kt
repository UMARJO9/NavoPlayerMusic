package tj.umar.navoplayer.core.domain.usecase

import tj.umar.navoplayer.core.domain.app.AppInfoProvider
import tj.umar.navoplayer.core.domain.model.AppInfo
import javax.inject.Inject

class GetAppInfoUseCase @Inject constructor(
    private val appInfoProvider: AppInfoProvider,
) {
    operator fun invoke(): AppInfo = appInfoProvider.appInfo()
}
