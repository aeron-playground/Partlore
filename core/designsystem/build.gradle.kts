plugins {
    id("partlore.android.library")
    id("partlore.android.compose")
    id("partlore.android.screenshots")
}

android {
    namespace = "dev.partlore.core.designsystem"
}

dependencies {
    api(project(":core:model"))
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material3.adaptive.navigation.suite)

    testImplementation(project(":core:testing"))
}
