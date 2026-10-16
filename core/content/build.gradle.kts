plugins {
    id("partlore.jvm.library")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(project(":core:model"))
    implementation(project(":core:packformat"))
    implementation(libs.androidx.sqlite.bundled)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(project(":tools:packer"))
    testImplementation(testFixtures(project(":tools:validator")))
    testImplementation(libs.kotlinx.coroutines.test)
}

val schemaDir = rootProject.layout.projectDirectory.dir("content/schema")

tasks.withType<Test>().configureEach {
    // Tests build real packs from test content with the real packer.
    systemProperty("partlore.schemaDir", schemaDir.asFile.absolutePath)
    inputs.dir(schemaDir).withPathSensitivity(PathSensitivity.RELATIVE)
    // RealPinoutTest packs the real content to check where pin 1 of each header lands.
    val contentDir = rootProject.layout.projectDirectory.dir("content")
    systemProperty("partlore.contentDir", contentDir.asFile.absolutePath)
    inputs.dir(contentDir).withPathSensitivity(PathSensitivity.RELATIVE)
}

tasks.register<Test>("speedTest") {
    group = "verification"
    description = "Packs 5,000 generated parts and checks that each screen reads in under 20 ms (CI)."
    testClassesDirs =
        sourceSets.test
            .get()
            .output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    filter { includeTestsMatching("*SpeedTest") }
    systemProperty("partlore.speedTest", "true")
    maxHeapSize = "1g"
    testLogging { showStandardStreams = true }
}
