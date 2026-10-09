package tj.umar.navoplayer.feature.widget.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tj.umar.navoplayer.feature.widget.update.GlanceWidgetRefresher
import tj.umar.navoplayer.feature.widget.update.WidgetRefresher

@Module
@InstallIn(SingletonComponent::class)
internal interface WidgetModule {

    @Binds
    fun bindsWidgetRefresher(refresher: GlanceWidgetRefresher): WidgetRefresher
}
