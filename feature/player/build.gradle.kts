plugins {
    alias(libs.plugins.navo.android.feature)
}

android {
    namespace = "tj.umar.navoplayer.feature.player"
}

dependencies {
    implementation(libs.androidx.activity.compose)
}
