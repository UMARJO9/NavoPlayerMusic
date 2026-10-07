package tj.umar.navoplayer.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")

        extensions.configure<LibraryExtension> {
            configureKotlinAndroid(this)
            testOptions.targetSdk = NAVO_TARGET_SDK
            testOptions.unitTests.isReturnDefaultValues = true
        }

        dependencies {
            add("testImplementation", libs.findLibrary("junit").get())
        }
    }
}
