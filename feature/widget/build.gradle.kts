plugins {
    alias(libs.plugins.navo.android.feature)
}

android {
    namespace = "tj.umar.navoplayer.feature.widget"
}

dependencies {
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.preview)
    debugImplementation(libs.androidx.glance.appwidget.preview)
}
