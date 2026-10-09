package tj.umar.navoplayer.core.player.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tj.umar.navoplayer.core.domain.playback.EqualizerController
import tj.umar.navoplayer.core.domain.playback.PlaybackController
import tj.umar.navoplayer.core.domain.playback.SleepTimerController
import tj.umar.navoplayer.core.player.controller.DefaultPlaybackController
import tj.umar.navoplayer.core.player.controller.QueueItemIdFactory
import tj.umar.navoplayer.core.player.controller.UuidQueueItemIdFactory
import tj.umar.navoplayer.core.player.equalizer.AndroidEqualizerProbe
import tj.umar.navoplayer.core.player.equalizer.DefaultEqualizerController
import tj.umar.navoplayer.core.player.equalizer.EqualizerProbe
import tj.umar.navoplayer.core.player.sleeptimer.DefaultSleepTimerController

@Module
@InstallIn(SingletonComponent::class)
internal interface PlayerModule {

    @Binds
    fun bindsPlaybackController(controller: DefaultPlaybackController): PlaybackController

    @Binds
    fun bindsQueueItemIdFactory(factory: UuidQueueItemIdFactory): QueueItemIdFactory

    @Binds
    fun bindsSleepTimerController(controller: DefaultSleepTimerController): SleepTimerController

    @Binds
    fun bindsEqualizerController(controller: DefaultEqualizerController): EqualizerController

    @Binds
    fun bindsEqualizerProbe(probe: AndroidEqualizerProbe): EqualizerProbe
}
