import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.UnitTestOptions
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidScreenshotsConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("io.github.takahirom.roborazzi")
            pluginManager.withPlugin("com.android.library") {
                extensions.configure<LibraryExtension> { testOptions.unitTests.configureRobolectric() }
            }
            pluginManager.withPlugin("com.android.application") {
                extensions.configure<ApplicationExtension> { testOptions.unitTests.configureRobolectric() }
            }
            dependencies {
                listOf(
                    "junit",
                    "robolectric",
                    "roborazzi",
                    "roborazzi-compose",
                    "androidx-activity-compose",
                    "androidx-compose-ui-test-junit4",
                ).forEach { add("testImplementation", lib(it)) }
                // Roborazzi brings Espresso 3.5.1, which calls an input API that Android 17 removed.
                add("testImplementation", lib("androidx-test-espresso-core"))
                add("testImplementation", lib("androidx-test-core"))
            }
            // `./gradlew check` also compares screenshots with the committed ones.
            tasks.named("check") {
                dependsOn(tasks.matching { it.name == "verifyRoborazziDebug" || it.name == "verifyRoborazziPlayDebug" })
            }
        }
    }
}

private fun UnitTestOptions.configureRobolectric() {
    isIncludeAndroidResources = true
    all {
        it.maxHeapSize = "1536m"
        // Robolectric's setup for Android 16+ images reads a JDK-internal class that Java 21 hides.
        it.jvmArgs("--add-exports=java.base/jdk.internal.access=ALL-UNNAMED")
        // Needed for drop shadows to render in screenshots.
        it.systemProperty("robolectric.pixelCopyRenderMode", "hardware")
    }
}
