plugins {
    alias(libs.plugins.navo.android.library)
    alias(libs.plugins.navo.hilt)
}

android {
    namespace = "tj.umar.navoplayer.core.mediastore"
}

dependencies {
    implementation(projects.core.common)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(projects.core.testing)
    testImplementation(libs.robolectric)
}
