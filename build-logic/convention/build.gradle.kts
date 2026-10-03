plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)

    // Same floor as the root build's classpath: AGP pulls in older versions with known
    // vulnerabilities (see gradle/libs.versions.toml).
    constraints {
        listOf(
            libs.bouncycastle.bcprov,
            libs.bouncycastle.bcpkix,
            libs.bouncycastle.bcutil,
            libs.jose4j,
            libs.jdom2,
            libs.commons.lang3,
            libs.httpclient,
        ).forEach { compileOnly(it) }
    }
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "partlore.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "partlore.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "partlore.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("androidScreenshots") {
            id = "partlore.android.screenshots"
            implementationClass = "AndroidScreenshotsConventionPlugin"
        }
        register("androidFeature") {
            id = "partlore.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("jvmLibrary") {
            id = "partlore.jvm.library"
            implementationClass = "JvmLibraryConventionPlugin"
        }
    }
}
