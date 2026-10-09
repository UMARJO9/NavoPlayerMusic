package tj.umar.navoplayer.core.player.artwork

fun interface ResumptionArtwork {
    fun render(trackId: Long): ByteArray?
}
