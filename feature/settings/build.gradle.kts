plugins {
    id("partlore.android.feature")
}

android {
    namespace = "dev.partlore.feature.settings"
}

dependencies {
    implementation(project(":core:userdata"))
}
