plugins {
    id("partlore.jvm.library")
}

dependencies {
    // Only the tests run SQL; the format itself is plain Kotlin.
    testImplementation(libs.androidx.sqlite.bundled)
}
