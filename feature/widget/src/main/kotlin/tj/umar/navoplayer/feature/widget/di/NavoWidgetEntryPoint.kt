package tj.umar.navoplayer.feature.widget.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tj.umar.navoplayer.core.domain.usecase.ObserveNowPlayingUseCase
import tj.umar.navoplayer.feature.widget.action.WidgetCommandHandler

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface NavoWidgetEntryPoint {
    fun observeNowPlaying(): ObserveNowPlayingUseCase
    fun widgetCommandHandler(): WidgetCommandHandler
}
