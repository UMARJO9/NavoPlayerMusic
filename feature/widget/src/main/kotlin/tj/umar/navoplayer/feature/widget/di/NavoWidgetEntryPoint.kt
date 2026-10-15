package tj.umar.navoplayer.feature.widget.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import tj.umar.navoplayer.core.common.dispatchers.DefaultDispatcher
import tj.umar.navoplayer.core.domain.usecase.ObserveNowPlayingUseCase
import tj.umar.navoplayer.core.domain.usecase.ObserveThemeModeUseCase
import tj.umar.navoplayer.core.ui.artwork.ArtworkLoader
import tj.umar.navoplayer.feature.widget.action.WidgetCommandHandler
import tj.umar.navoplayer.feature.widget.update.NavoWidgetUpdater

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface NavoWidgetEntryPoint {
    fun observeNowPlaying(): ObserveNowPlayingUseCase
    fun observeThemeMode(): ObserveThemeModeUseCase
    fun widgetCommandHandler(): WidgetCommandHandler
    fun widgetUpdater(): NavoWidgetUpdater
    fun artworkLoader(): ArtworkLoader

    @DefaultDispatcher
    fun defaultDispatcher(): CoroutineDispatcher
}
