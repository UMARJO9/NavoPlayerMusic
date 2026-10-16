package tj.umar.navoplayer.core.designsystem.glass

import android.os.Build

internal enum class GlassTier { Refraction, Blur, Tint }

internal fun glassTier(sdkInt: Int, inspection: Boolean): GlassTier = when {
    inspection || sdkInt < Build.VERSION_CODES.S -> GlassTier.Tint
    sdkInt < Build.VERSION_CODES.TIRAMISU -> GlassTier.Blur
    else -> GlassTier.Refraction
}
