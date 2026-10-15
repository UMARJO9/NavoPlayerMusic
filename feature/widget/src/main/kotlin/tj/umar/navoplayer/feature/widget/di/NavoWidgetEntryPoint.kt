package tj.umar.navoplayer.feature.widget.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tj.umar.navoplayer.core.domain.usecase.ObserveNowPlayingUseCase
import tj.umar.navoplayer.core.domain.usecase.ObserveThemeModeUseCase
import tj.umar.navoplayer.feature.widget.action.WidgetCommandHandler
import tj.umar.navoplayer.feature.widget.update.NavoWidgetUpdater

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface NavoWidgetEntryPoint {
    fun observeNowPlaying(): ObserveNowPlayingUseCase
    fun observeThemeMode(): ObserveThemeModeUseCase
    fun widgetCommandHandler(): WidgetCommandHandler
    fun widgetUpdater(): NavoWidgetUpdater
}
