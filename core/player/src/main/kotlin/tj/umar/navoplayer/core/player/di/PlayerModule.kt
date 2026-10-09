package tj.umar.navoplayer.core.player.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tj.umar.navoplayer.core.domain.playback.PlaybackController
import tj.umar.navoplayer.core.player.controller.DefaultPlaybackController
import tj.umar.navoplayer.core.player.controller.QueueItemIdFactory
import tj.umar.navoplayer.core.player.controller.UuidQueueItemIdFactory

@Module
@InstallIn(SingletonComponent::class)
internal interface PlayerModule {

    @Binds
    fun bindsPlaybackController(controller: DefaultPlaybackController): PlaybackController

    @Binds
    fun bindsQueueItemIdFactory(factory: UuidQueueItemIdFactory): QueueItemIdFactory
}
