plugins {
    alias(libs.plugins.navo.android.feature)
}

android {
    namespace = "tj.umar.navoplayer.feature.library"
}

dependencies {
    implementation(libs.androidx.activity.compose)
}
