package tj.umar.navoplayer.feature.player.nowplaying

import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.Track

internal val previewNowPlayingState = NowPlayingState(
    isLoading = false,
    track = Track(2, "Ҷавонӣ", "Daler Nazarov", null, 11, 101, 252_000, 2, "content://media/2", "Music/Navo", null),
    nextTrack = Track(1, "Утро в Варзобе", "Navo Band", "Варзоб", 10, 100, 185_000, 1, "content://media/1", "Music/Navo", null),
    source = PlaybackSource.AllTracks,
    isPlaying = true,
    positionMs = 97_000,
    durationMs = 252_000,
)
