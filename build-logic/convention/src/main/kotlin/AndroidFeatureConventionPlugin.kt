import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("partlore.android.library")
            pluginManager.apply("partlore.android.compose")
            pluginManager.apply("partlore.android.screenshots")
            dependencies {
                add("implementation", project(":core:designsystem"))
                add("implementation", project(":core:model"))
                add("implementation", lib("androidx-lifecycle-runtime-compose"))
                add("implementation", lib("androidx-lifecycle-viewmodel-compose"))
                add("implementation", platform(lib("koin-bom")))
                add("implementation", lib("koin-compose-viewmodel"))
                add("testImplementation", project(":core:testing"))
                add("testImplementation", lib("kotlinx-coroutines-test"))
                add("testImplementation", lib("turbine"))
            }
        }
    }
}
