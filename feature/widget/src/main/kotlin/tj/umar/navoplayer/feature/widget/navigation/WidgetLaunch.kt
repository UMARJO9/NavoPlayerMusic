package tj.umar.navoplayer.feature.widget.navigation

import android.content.Context
import android.content.Intent

const val ACTION_OPEN_NOW_PLAYING = "tj.umar.navoplayer.action.OPEN_NOW_PLAYING"

fun Context.widgetLaunchIntent(openNowPlaying: Boolean): Intent {
    val intent = packageManager.getLaunchIntentForPackage(packageName) ?: Intent().setPackage(packageName)
    if (openNowPlaying) intent.action = ACTION_OPEN_NOW_PLAYING
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    return intent
}

fun Intent.isOpenNowPlayingRequest(): Boolean = action == ACTION_OPEN_NOW_PLAYING
