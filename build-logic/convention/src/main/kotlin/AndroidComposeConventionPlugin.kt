import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
            pluginManager.withPlugin("com.android.library") {
                extensions.configure<LibraryExtension> { buildFeatures.compose = true }
            }
            pluginManager.withPlugin("com.android.application") {
                extensions.configure<ApplicationExtension> { buildFeatures.compose = true }
            }
            dependencies {
                add("implementation", platform(lib("androidx-compose-bom")))
                add("testImplementation", platform(lib("androidx-compose-bom")))
                add("implementation", lib("androidx-compose-ui-tooling-preview"))
                add("debugImplementation", lib("androidx-compose-ui-tooling"))
            }
        }
    }
}
