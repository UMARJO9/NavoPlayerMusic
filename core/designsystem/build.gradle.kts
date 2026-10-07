plugins {
    alias(libs.plugins.navo.android.library)
    alias(libs.plugins.navo.android.compose)
}

android {
    namespace = "tj.umar.navoplayer.core.designsystem"
}

dependencies {
    api(libs.androidx.compose.material3)
}
