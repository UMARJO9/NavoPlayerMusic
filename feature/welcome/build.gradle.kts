plugins {
    alias(libs.plugins.navo.android.feature)
}

android {
    namespace = "tj.umar.navoplayer.feature.welcome"
}

dependencies {
    implementation(libs.androidx.activity.compose)
}
