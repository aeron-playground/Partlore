plugins {
    id("partlore.android.feature")
}

android {
    namespace = "dev.partlore.feature.pinout"
}

dependencies {
    implementation(project(":core:content"))
    implementation(project(":core:userdata"))
}
