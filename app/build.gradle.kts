plugins {
    alias(libs.plugins.navo.android.application)
    alias(libs.plugins.navo.android.compose)
    alias(libs.plugins.navo.hilt)
}

android {
    namespace = "tj.umar.navoplayer"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "tj.umar.navoplayer"
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.data)
    implementation(projects.core.designsystem)
    implementation(projects.core.domain)
    implementation(projects.core.player)
    implementation(projects.core.ui)
    implementation(projects.feature.library)
    implementation(projects.feature.player)
    implementation(projects.feature.playlists)
    implementation(projects.feature.search)
    implementation(projects.feature.settings)
    implementation(projects.feature.welcome)
    implementation(projects.feature.widget)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.navigation.compose)
}
