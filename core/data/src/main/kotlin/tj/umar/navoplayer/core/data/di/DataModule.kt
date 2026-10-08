package tj.umar.navoplayer.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tj.umar.navoplayer.core.data.repository.DataStoreSettingsRepository
import tj.umar.navoplayer.core.data.repository.MediaStoreTrackRepository
import tj.umar.navoplayer.core.data.repository.RoomFavoritesRepository
import tj.umar.navoplayer.core.data.repository.RoomPlaylistRepository
import tj.umar.navoplayer.core.domain.repository.FavoritesRepository
import tj.umar.navoplayer.core.domain.repository.PlaylistRepository
import tj.umar.navoplayer.core.domain.repository.SettingsRepository
import tj.umar.navoplayer.core.domain.repository.TrackRepository

@Module
@InstallIn(SingletonComponent::class)
internal interface DataModule {

    @Binds
    fun bindsTrackRepository(repository: MediaStoreTrackRepository): TrackRepository

    @Binds
    fun bindsPlaylistRepository(repository: RoomPlaylistRepository): PlaylistRepository

    @Binds
    fun bindsFavoritesRepository(repository: RoomFavoritesRepository): FavoritesRepository

    @Binds
    fun bindsSettingsRepository(repository: DataStoreSettingsRepository): SettingsRepository
}
