plugins {
    id("partlore.android.feature")
}

android {
    namespace = "dev.partlore.feature.onboarding"
}

dependencies {
    implementation(project(":core:userdata"))
}
