plugins {
    alias(libs.plugins.navo.jvm.library)
    alias(libs.plugins.navo.hilt)
}

dependencies {
    testImplementation(libs.kotlinx.coroutines.test)
}
