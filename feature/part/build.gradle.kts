plugins {
    id("partlore.android.feature")
}

android {
    namespace = "dev.partlore.feature.part"
}

dependencies {
    implementation(project(":core:content"))
}
