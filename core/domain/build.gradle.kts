plugins {
    alias(libs.plugins.navo.jvm.library)
    alias(libs.plugins.navo.hilt)
}

dependencies {
    api(projects.core.common)
    api(libs.kotlinx.coroutines.core)

    testImplementation(projects.core.testing)
}
