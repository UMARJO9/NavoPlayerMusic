plugins {
    alias(libs.plugins.navo.android.library)
    alias(libs.plugins.navo.hilt)
}

android {
    namespace = "tj.umar.navoplayer.core.player"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.domain)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.guava)

    testImplementation(projects.core.testing)
    testImplementation(libs.robolectric)
}
