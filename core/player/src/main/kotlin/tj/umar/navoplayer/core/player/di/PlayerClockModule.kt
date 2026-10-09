package tj.umar.navoplayer.core.player.di

import android.os.SystemClock
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tj.umar.navoplayer.core.common.time.ElapsedRealtimeClock
import tj.umar.navoplayer.core.common.time.NavoClock

@Module
@InstallIn(SingletonComponent::class)
internal object PlayerClockModule {

    @Provides
    @ElapsedRealtimeClock
    fun provideElapsedRealtimeClock(): NavoClock = NavoClock(SystemClock::elapsedRealtime)
}
