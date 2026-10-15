package tj.umar.navoplayer

import android.app.Application
import android.content.res.Configuration
import dagger.hilt.android.HiltAndroidApp
import tj.umar.navoplayer.feature.widget.update.NavoWidgetUpdater
import javax.inject.Inject

@HiltAndroidApp
class NavoApplication : Application() {

    @Inject
    lateinit var widgetUpdater: NavoWidgetUpdater

    override fun onCreate() {
        super.onCreate()
        widgetUpdater.onSystemNightModeChanged(resources.configuration.isNightMode())
        widgetUpdater.start()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        widgetUpdater.onSystemNightModeChanged(newConfig.isNightMode())
    }
}

private fun Configuration.isNightMode(): Boolean =
    (uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
