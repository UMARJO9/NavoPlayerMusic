package tj.umar.navoplayer.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        val extension = extensions.findByType<ApplicationExtension>()
            ?: extensions.findByType<LibraryExtension>()
            ?: error("navo.android.compose requires an Android application or library plugin")
        configureAndroidCompose(extension)
    }
}
