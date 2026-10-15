package tj.umar.navoplayer.splash

import android.content.Context
import android.provider.Settings

internal fun Context.animatorDurationScale(): Float =
    Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
