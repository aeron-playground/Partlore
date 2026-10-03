plugins {
    id("partlore.android.library")
    id("partlore.android.compose")
    id("partlore.android.screenshots")
}

android {
    namespace = "dev.partlore.core.designsystem"
}

dependencies {
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.material3)
}
