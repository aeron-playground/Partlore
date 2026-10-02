plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "dev.partlore.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "dev.partlore.app"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.0.1"
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
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
