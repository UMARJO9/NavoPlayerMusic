package tj.umar.navoplayer.core.domain.model

enum class TransportCommand { TogglePlayPause, SkipToNext, SkipToPrevious }

enum class TransportOutcome { Sent, NoActiveSession }
