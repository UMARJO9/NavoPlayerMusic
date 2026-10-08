package tj.umar.navoplayer.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tj.umar.navoplayer.BuildConfig
import tj.umar.navoplayer.core.domain.app.AppInfoProvider
import tj.umar.navoplayer.core.domain.model.AppInfo
import javax.inject.Inject

internal class BuildConfigAppInfoProvider @Inject constructor() : AppInfoProvider {
    override fun appInfo(): AppInfo = AppInfo(versionName = BuildConfig.VERSION_NAME)
}

@Module
@InstallIn(SingletonComponent::class)
internal interface AppInfoModule {

    @Binds
    fun bindsAppInfoProvider(provider: BuildConfigAppInfoProvider): AppInfoProvider
}
