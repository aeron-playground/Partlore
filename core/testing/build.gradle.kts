plugins {
    id("partlore.android.library")
}

android {
    namespace = "dev.partlore.core.testing"
}

dependencies {
    api(project(":core:content"))
    api(project(":core:userdata"))
    api(libs.junit)
    api(libs.robolectric)
    api(libs.kotlinx.coroutines.test)
    implementation(libs.androidx.activity.compose)
}
