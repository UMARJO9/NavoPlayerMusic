plugins {
    alias(libs.plugins.navo.android.library)
    alias(libs.plugins.navo.android.compose)
}

android {
    namespace = "tj.umar.navoplayer.core.designsystem"
}

dependencies {
    api(libs.androidx.compose.material3)
    implementation(libs.kyant.backdrop)
    implementation(libs.kyant.shapes)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
}
