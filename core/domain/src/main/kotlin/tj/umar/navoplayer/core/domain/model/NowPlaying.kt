package tj.umar.navoplayer.core.domain.model

data class NowPlaying(val track: Track?, val isPlaying: Boolean) {
    companion object {
        val Idle = NowPlaying(track = null, isPlaying = false)
    }
}
