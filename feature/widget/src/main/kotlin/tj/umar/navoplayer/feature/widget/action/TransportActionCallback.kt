package tj.umar.navoplayer.feature.widget.action

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import dagger.hilt.android.EntryPointAccessors
import tj.umar.navoplayer.core.domain.model.TransportCommand
import tj.umar.navoplayer.feature.widget.di.NavoWidgetEntryPoint

internal val TransportCommandKey = ActionParameters.Key<String>("transport_command")

internal class TransportActionCallback : ActionCallback {

    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val command = parameters[TransportCommandKey]
            ?.let { name -> TransportCommand.entries.firstOrNull { it.name == name } }
            ?: return
        EntryPointAccessors.fromApplication(context, NavoWidgetEntryPoint::class.java)
            .widgetCommandHandler()
            .handle(command)
    }
}
