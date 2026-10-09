package tj.umar.navoplayer.feature.widget.action

import tj.umar.navoplayer.core.domain.model.TransportCommand
import tj.umar.navoplayer.core.domain.model.TransportOutcome
import tj.umar.navoplayer.core.domain.usecase.SendTransportCommandUseCase
import tj.umar.navoplayer.feature.widget.update.WidgetRefresher
import javax.inject.Inject

internal class WidgetCommandHandler @Inject constructor(
    private val sendTransportCommand: SendTransportCommandUseCase,
    private val refresher: WidgetRefresher,
) {
    suspend fun handle(command: TransportCommand) {
        if (sendTransportCommand(command) == TransportOutcome.NoActiveSession) refresher.refresh()
    }
}
