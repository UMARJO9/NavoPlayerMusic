package tj.umar.navoplayer.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        extensions.configure<ComposeCompilerGradlePluginExtension> {
            stabilityConfigurationFiles.add(
                isolated.rootProject.projectDirectory.file("compose_stability.conf"),
            )
        }

        pluginManager.withPlugin("com.android.application") {
            extensions.configure<ApplicationExtension> { configureAndroidCompose(this) }
        }
        pluginManager.withPlugin("com.android.library") {
            extensions.configure<LibraryExtension> { configureAndroidCompose(this) }
        }
    }
}
