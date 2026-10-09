package tj.umar.navoplayer

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import tj.umar.navoplayer.feature.widget.update.NavoWidgetUpdater
import javax.inject.Inject

@HiltAndroidApp
class NavoApplication : Application() {

    @Inject
    lateinit var widgetUpdater: NavoWidgetUpdater

    override fun onCreate() {
        super.onCreate()
        widgetUpdater.start()
    }
}
