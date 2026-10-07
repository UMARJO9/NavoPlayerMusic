plugins {
    alias(libs.plugins.navo.jvm.library)
}

dependencies {
    api(projects.core.common)
    api(projects.core.domain)
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
    api(libs.turbine)
}
