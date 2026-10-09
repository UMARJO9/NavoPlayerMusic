package tj.umar.navoplayer.feature.widget.update

import android.content.Context
import androidx.glance.appwidget.updateAll
import dagger.hilt.android.qualifiers.ApplicationContext
import tj.umar.navoplayer.feature.widget.NavoWidget
import javax.inject.Inject

internal class GlanceWidgetRefresher @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : WidgetRefresher {

    override suspend fun refresh() {
        NavoWidget().updateAll(context)
    }
}
