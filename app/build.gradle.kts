plugins {
    id("partlore.android.application")
    id("partlore.android.compose")
    id("partlore.android.screenshots")
    alias(libs.plugins.kotlin.serialization)
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

// Release signing only happens in the release workflow, which provides the key through these
// variables. Everywhere else, release builds stay unsigned.
val releaseKeystoreFile = providers.environmentVariable("RELEASE_KEYSTORE_FILE").orNull
val releaseKeystorePassword = providers.environmentVariable("RELEASE_KEYSTORE_PASSWORD").orNull

android {
    namespace = "dev.partlore.app"

    defaultConfig {
        applicationId = "dev.partlore.app"
        versionCode = appVersionCode
        versionName = appVersion
    }

    // play: Google Play build. foss: F-Droid build, open-source dependencies only.
    flavorDimensions += "distribution"
    productFlavors {
        create("play") { dimension = "distribution" }
        create("foss") { dimension = "distribution" }
    }

    signingConfigs {
        if (releaseKeystoreFile != null && releaseKeystorePassword != null) {
            create("release") {
                storeFile = file(releaseKeystoreFile)
                storePassword = releaseKeystorePassword
                keyAlias = "partlore"
                // PKCS12 keystores use one password for the store and the key.
                keyPassword = releaseKeystorePassword
            }
        }
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            signingConfig = signingConfigs.findByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:model"))
    implementation(project(":core:userdata"))
    implementation(project(":core:content"))
    implementation(project(":feature:library"))
    implementation(project(":feature:part"))
    implementation(project(":feature:onboarding"))
    implementation(project(":feature:settings"))
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)

    testImplementation(project(":core:testing"))
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}

androidComponents {
    onVariants { variant ->
        // Debug builds carry drafts so they can be tried; release builds carry checked parts only.
        val kind = if (variant.buildType == "debug") "Preview" else "Release"
        val copy =
            tasks.register<CopyPackTask>("copy${variant.name.replaceFirstChar(Char::uppercase)}Pack") {
                dependsOn(":tools:packer:pack${kind}Asset")
                packDir.set(rootProject.layout.projectDirectory.dir("tools/packer/build/packs/${kind.lowercase()}"))
            }
        variant.sources.assets?.addGeneratedSourceDirectory(copy, CopyPackTask::outputDir)
    }
}
