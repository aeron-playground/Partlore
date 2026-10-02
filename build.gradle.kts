buildscript {
    dependencies {
        // Raise libraries that AGP puts on the build classpath to versions with security fixes.
        constraints {
            listOf(
                libs.bouncycastle.bcprov,
                libs.bouncycastle.bcpkix,
                libs.bouncycastle.bcutil,
                libs.jose4j,
                libs.jdom2,
                libs.commons.lang3,
            ).forEach { add("classpath", it.get().toString()) }
        }
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.detekt)
}

spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**")
        ktlint(libs.versions.ktlint.get())
            .setEditorConfigPath(rootProject.file(".editorconfig"))
            // Also set in .editorconfig for the IDE and the ktlint CLI. Spotless 8.10 does not
            // pick this property up from .editorconfig, so it is repeated here.
            .editorConfigOverride(
                mapOf("ktlint_function_naming_ignore_when_annotated_with" to "Composable"),
            ).customRuleSets(
                listOf(
                    libs.compose.rules.ktlint
                        .get()
                        .toString(),
                ),
            )
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("**/build/**")
        ktlint(libs.versions.ktlint.get())
            .setEditorConfigPath(rootProject.file(".editorconfig"))
    }
}

// One detekt run over every module's sources.
detekt {
    buildUponDefaultConfig = true
    parallel = true
    config.setFrom(files("config/detekt/detekt.yml"))
    source.setFrom(
        fileTree(rootDir) {
            include("**/src/**/*.kt")
            exclude("**/build/**")
        },
    )
}

dependencies {
    detektPlugins(libs.compose.rules.detekt)
}
