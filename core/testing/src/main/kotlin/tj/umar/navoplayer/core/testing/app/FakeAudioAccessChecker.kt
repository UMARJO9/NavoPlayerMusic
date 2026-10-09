package tj.umar.navoplayer.core.testing.app

import tj.umar.navoplayer.core.domain.app.AudioAccessChecker

class FakeAudioAccessChecker(var granted: Boolean = true) : AudioAccessChecker {
    override fun hasAudioAccess(): Boolean = granted
}
