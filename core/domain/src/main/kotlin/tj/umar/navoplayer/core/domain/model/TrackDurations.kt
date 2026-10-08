package tj.umar.navoplayer.core.domain.model

private const val MILLIS_PER_MINUTE = 60_000L

fun List<Track>.totalDurationMinutes(): Int = sumOf { it.durationMs }.toRoundedMinutes()

fun PlaylistSummary.durationMinutes(): Int = durationMs.toRoundedMinutes()

fun FavoritesSummary.durationMinutes(): Int = durationMs.toRoundedMinutes()

private fun Long.toRoundedMinutes(): Int = ((this + MILLIS_PER_MINUTE / 2) / MILLIS_PER_MINUTE).toInt()
