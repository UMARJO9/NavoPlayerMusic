package tj.umar.navoplayer.core.domain.usecase

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.SleepTimer
import tj.umar.navoplayer.core.testing.playback.FakeSleepTimerController
import tj.umar.navoplayer.core.testing.playback.SleepTimerCommand
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class SleepTimerUseCasesTest {

    private val controller = FakeSleepTimerController()

    @Test
    fun `observe forwards controller timer`() = runTest {
        controller.timer.value = SleepTimer.EndOfTrack

        ObserveSleepTimerUseCase(controller)().test {
            assertEquals(SleepTimer.EndOfTrack, awaitItem())
        }
    }

    @Test
    fun `start forwards duration`() {
        assertTrue(StartSleepTimerUseCase(controller)(15.minutes))
        assertEquals(listOf(SleepTimerCommand.Start(15.minutes)), controller.commands)
    }

    @Test
    fun `start without positive duration is rejected`() {
        val start = StartSleepTimerUseCase(controller)

        assertFalse(start(Duration.ZERO))
        assertFalse(start((-1).minutes))
        assertTrue(controller.commands.isEmpty())
    }

    @Test
    fun `start reports controller rejection`() {
        controller.acceptStart = false

        assertFalse(StartSleepTimerUseCase(controller)(5.minutes))
        assertFalse(StartEndOfTrackSleepTimerUseCase(controller)())
    }

    @Test
    fun `end of track and cancel forward`() {
        assertTrue(StartEndOfTrackSleepTimerUseCase(controller)())
        CancelSleepTimerUseCase(controller)()

        assertEquals(listOf(SleepTimerCommand.StartEndOfTrack, SleepTimerCommand.Cancel), controller.commands)
        assertEquals(SleepTimer.Off, controller.timer.value)
    }
}
