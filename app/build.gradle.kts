plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// version.txt is updated by release-please. The version code is derived from it, so it always
// grows: 1.2.3 -> 1_002_003. Before the first release (0.0.0) it is 1, the lowest Android allows.
val appVersion =
    providers
        .fileContents(rootProject.layout.projectDirectory.file("version.txt"))
        .asText
        .get()
        .trim()
val appVersionCode =
    appVersion
        .split(".")
        .map(String::toInt)
        .let { (major, minor, patch) -> major * 1_000_000 + minor * 1_000 + patch }
        .coerceAtLeast(1)

android {
    namespace = "dev.partlore.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "dev.partlore.app"
        minSdk = 26
        targetSdk = 37
        versionCode = appVersionCode
        versionName = appVersion
    }

    // play: Google Play build. foss: F-Droid build, open-source dependencies only.
    flavorDimensions += "distribution"
    productFlavors {
        create("play") { dimension = "distribution" }
        create("foss") { dimension = "distribution" }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    lint {
        warningsAsErrors = true
        abortOnError = true
        checkDependencies = true
        // Version updates come from the dependency bot, not from lint.
        disable += setOf("GradleDependency", "NewerVersionAvailable", "AndroidGradlePluginVersion")
    }
}

dependencies {
    // Android Lint runs with its own classpath. Raise the same vulnerable libraries there.
    constraints {
        listOf(
            libs.bouncycastle.bcprov,
            libs.bouncycastle.bcpkix,
            libs.bouncycastle.bcutil,
            libs.commons.lang3,
            libs.httpclient,
        ).forEach { add("androidLintTool", it) }
    }

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
