package tj.umar.navoplayer.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tj.umar.navoplayer.core.data.repository.MediaStoreTrackRepository
import tj.umar.navoplayer.core.domain.repository.TrackRepository

@Module
@InstallIn(SingletonComponent::class)
internal interface DataModule {

    @Binds
    fun bindsTrackRepository(repository: MediaStoreTrackRepository): TrackRepository
}
