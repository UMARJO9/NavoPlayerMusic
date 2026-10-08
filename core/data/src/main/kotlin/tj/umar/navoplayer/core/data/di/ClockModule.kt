package tj.umar.navoplayer.core.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tj.umar.navoplayer.core.common.time.NavoClock

@Module
@InstallIn(SingletonComponent::class)
internal object ClockModule {

    @Provides
    fun provideClock(): NavoClock = NavoClock(System::currentTimeMillis)
}
