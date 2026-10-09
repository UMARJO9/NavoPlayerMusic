package tj.umar.navoplayer.feature.widget

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import tj.umar.navoplayer.core.designsystem.medallion.MedallionPalettes
import tj.umar.navoplayer.core.designsystem.medallion.renderBitmap
import tj.umar.navoplayer.core.domain.model.TransportCommand
import tj.umar.navoplayer.feature.widget.action.TransportActionCallback
import tj.umar.navoplayer.feature.widget.action.TransportCommandKey
import tj.umar.navoplayer.feature.widget.di.NavoWidgetEntryPoint
import tj.umar.navoplayer.feature.widget.navigation.widgetLaunchIntent
import kotlin.math.roundToInt

private const val ARTWORK_DP = 64

class NavoWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(180.dp, 48.dp),
            DpSize(250.dp, 48.dp),
            DpSize(250.dp, 110.dp),
        ),
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(context, NavoWidgetEntryPoint::class.java)
        val states = entryPoint.observeNowPlaying().invoke().map { it.toWidgetUiState() }
        val initial = states.first()
        val artworkPx = (ARTWORK_DP * context.resources.displayMetrics.density).roundToInt()
        provideContent {
            val state by states.collectAsState(initial)
            val trackId = (state as? NavoWidgetUiState.Playing)?.trackId
            val artwork = remember(trackId) { trackId?.let { MedallionPalettes.forKey(it).renderBitmap(artworkPx) } }
            NavoWidgetContent(state = state, artwork = artwork, actions = widgetActions(context, state))
        }
    }
}

private fun widgetActions(context: Context, state: NavoWidgetUiState): NavoWidgetActions {
    val playing = state is NavoWidgetUiState.Playing
    return NavoWidgetActions(
        open = actionStartActivity(context.widgetLaunchIntent(openNowPlaying = playing)),
        togglePlayPause = if (playing) transportAction(TransportCommand.TogglePlayPause) else null,
        previous = if (playing) transportAction(TransportCommand.SkipToPrevious) else null,
        next = if (playing) transportAction(TransportCommand.SkipToNext) else null,
    )
}

private fun transportAction(command: TransportCommand) =
    actionRunCallback<TransportActionCallback>(actionParametersOf(TransportCommandKey to command.name))
