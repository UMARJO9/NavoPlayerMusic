package tj.umar.navoplayer.core.mediastore.di

import android.content.Context
import android.os.Build
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import tj.umar.navoplayer.core.mediastore.artwork.ArtworkDecoder
import tj.umar.navoplayer.core.mediastore.artwork.AudioArtworkSource
import tj.umar.navoplayer.core.mediastore.artwork.LegacyArtworkDecoder
import tj.umar.navoplayer.core.mediastore.artwork.MediaStoreArtworkSource
import tj.umar.navoplayer.core.mediastore.artwork.ThumbnailArtworkDecoder
import tj.umar.navoplayer.core.mediastore.audio.AudioMediaSource
import tj.umar.navoplayer.core.mediastore.audio.MediaStoreAudioSource

@Module
@InstallIn(SingletonComponent::class)
internal interface MediaStoreModule {

    @Binds
    fun bindsAudioMediaSource(source: MediaStoreAudioSource): AudioMediaSource

    @Binds
    fun bindsAudioArtworkSource(source: MediaStoreArtworkSource): AudioArtworkSource

    companion object {

        @Provides
        fun providesArtworkDecoder(@ApplicationContext context: Context): ArtworkDecoder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ThumbnailArtworkDecoder(context)
            } else {
                LegacyArtworkDecoder(context)
            }
    }
}
