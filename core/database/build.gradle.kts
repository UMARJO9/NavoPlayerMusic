plugins {
    alias(libs.plugins.navo.android.library)
    alias(libs.plugins.navo.android.room)
    alias(libs.plugins.navo.hilt)
}

android {
    namespace = "tj.umar.navoplayer.core.database"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(projects.core.testing)
    testImplementation(libs.robolectric)
}
