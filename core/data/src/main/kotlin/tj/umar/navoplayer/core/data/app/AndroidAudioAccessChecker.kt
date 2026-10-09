package tj.umar.navoplayer.core.data.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import tj.umar.navoplayer.core.domain.app.AudioAccessChecker
import javax.inject.Inject

internal class AndroidAudioAccessChecker @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : AudioAccessChecker {

    override fun hasAudioAccess(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }
}
