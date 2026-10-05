plugins {
    id("partlore.android.feature")
}

android {
    namespace = "dev.partlore.feature.library"
}

dependencies {
    implementation(project(":core:content"))
}
