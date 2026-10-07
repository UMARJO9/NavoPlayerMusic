plugins {
    alias(libs.plugins.navo.android.library)
    alias(libs.plugins.navo.android.compose)
}

android {
    namespace = "tj.umar.navoplayer.core.ui"
}

dependencies {
    implementation(projects.core.common)

    api(libs.androidx.lifecycle.viewmodel.ktx)
    api(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.lifecycle.runtime.compose)

    testImplementation(projects.core.testing)
}
