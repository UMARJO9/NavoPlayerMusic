package tj.umar.navoplayer.feature.widget.update

internal fun interface WidgetRefresher {
    suspend fun refresh()
}
