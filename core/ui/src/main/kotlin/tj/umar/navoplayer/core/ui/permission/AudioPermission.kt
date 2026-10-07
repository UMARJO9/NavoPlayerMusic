package tj.umar.navoplayer.core.ui.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

val audioReadPermission: String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

fun Context.hasAudioReadPermission(): Boolean =
    checkSelfPermission(audioReadPermission) == PackageManager.PERMISSION_GRANTED
