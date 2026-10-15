package tj.umar.navoplayer.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tj.umar.navoplayer.artwork.MediaStoreArtworkLoader
import tj.umar.navoplayer.core.ui.artwork.ArtworkLoader

@Module
@InstallIn(SingletonComponent::class)
internal interface ArtworkLoaderModule {

    @Binds
    fun bindsArtworkLoader(loader: MediaStoreArtworkLoader): ArtworkLoader
}
