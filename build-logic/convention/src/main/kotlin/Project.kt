import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

internal const val COMPILE_SDK = 37
internal const val TARGET_SDK = 37
internal const val MIN_SDK = 26

// Version updates come from the dependency bot, not from lint.
internal val LINT_DISABLED = setOf("GradleDependency", "NewerVersionAvailable", "AndroidGradlePluginVersion")

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun Project.lib(alias: String): Provider<MinimalExternalModuleDependency> = libs.findLibrary(alias).get()
