plugins {
    alias(libs.plugins.navo.android.library)
    alias(libs.plugins.navo.hilt)
}

android {
    namespace = "tj.umar.navoplayer.core.data"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.domain)
    implementation(projects.core.mediastore)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(projects.core.testing)
}
