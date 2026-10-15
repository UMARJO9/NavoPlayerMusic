package tj.umar.navoplayer.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tj.umar.navoplayer.artwork.CoverOrMedallionPlaybackArtwork
import tj.umar.navoplayer.core.player.artwork.PlaybackArtwork

@Module
@InstallIn(SingletonComponent::class)
internal interface PlaybackArtworkModule {

    @Binds
    fun bindsPlaybackArtwork(artwork: CoverOrMedallionPlaybackArtwork): PlaybackArtwork
}
