package tj.umar.navoplayer.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import tj.umar.navoplayer.core.database.NAVO_DATABASE_NAME
import tj.umar.navoplayer.core.database.NavoDatabase
import tj.umar.navoplayer.core.database.dao.FavoriteDao
import tj.umar.navoplayer.core.database.dao.PlaybackQueueDao
import tj.umar.navoplayer.core.database.dao.PlaylistDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NavoDatabase =
        Room.databaseBuilder(context, NavoDatabase::class.java, NAVO_DATABASE_NAME).build()

    @Provides
    fun providePlaylistDao(database: NavoDatabase): PlaylistDao = database.playlistDao()

    @Provides
    fun provideFavoriteDao(database: NavoDatabase): FavoriteDao = database.favoriteDao()

    @Provides
    fun providePlaybackQueueDao(database: NavoDatabase): PlaybackQueueDao = database.playbackQueueDao()
}
