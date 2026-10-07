import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "tj.umar.navoplayer.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = libs.plugins.navo.android.application.get().pluginId
            implementationClass = "tj.umar.navoplayer.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("androidCompose") {
            id = libs.plugins.navo.android.compose.get().pluginId
            implementationClass = "tj.umar.navoplayer.buildlogic.AndroidComposeConventionPlugin"
        }
    }
}
