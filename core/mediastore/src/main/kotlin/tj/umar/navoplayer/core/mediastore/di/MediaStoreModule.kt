package tj.umar.navoplayer.core.mediastore.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tj.umar.navoplayer.core.mediastore.audio.AudioMediaSource
import tj.umar.navoplayer.core.mediastore.audio.MediaStoreAudioSource

@Module
@InstallIn(SingletonComponent::class)
internal interface MediaStoreModule {

    @Binds
    fun bindsAudioMediaSource(source: MediaStoreAudioSource): AudioMediaSource
}
