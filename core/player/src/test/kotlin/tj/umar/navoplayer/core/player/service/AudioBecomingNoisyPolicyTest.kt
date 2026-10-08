package tj.umar.navoplayer.core.player.service

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.MinTrackDuration
import tj.umar.navoplayer.core.domain.model.UserSettings
import tj.umar.navoplayer.core.domain.usecase.ObserveSettingsUseCase
import tj.umar.navoplayer.core.testing.repository.FakeSettingsRepository

class AudioBecomingNoisyPolicyTest {

    private val settings = FakeSettingsRepository()
    private val policy = AudioBecomingNoisyPolicy(ObserveSettingsUseCase(settings))

    @Test
    fun `follows the setting and ignores unrelated changes`() = runTest {
        policy.pauseOnDisconnect().test {
            assertEquals(true, awaitItem())
            settings.emit(UserSettings(minTrackDuration = MinTrackDuration.TenSeconds))
            expectNoEvents()
            settings.emit(UserSettings(pauseOnHeadphonesDisconnect = false))
            assertEquals(false, awaitItem())
        }
    }

    @Test
    fun `error falls back to pausing`() = runTest {
        settings.observeError = IllegalStateException("broken")

        policy.pauseOnDisconnect().test {
            assertEquals(true, awaitItem())
            awaitComplete()
        }
    }
}
