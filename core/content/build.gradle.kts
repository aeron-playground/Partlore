plugins {
    id("partlore.jvm.library")
}

dependencies {
    api(project(":core:model"))
    implementation(project(":core:packformat"))
    implementation(libs.androidx.sqlite.bundled)
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(project(":tools:packer"))
    testImplementation(testFixtures(project(":tools:validator")))
    testImplementation(libs.kotlinx.coroutines.test)
}

val schemaDir = rootProject.layout.projectDirectory.dir("content/schema")

tasks.withType<Test>().configureEach {
    // Tests build real packs from test content with the real packer.
    systemProperty("partlore.schemaDir", schemaDir.asFile.absolutePath)
    inputs.dir(schemaDir).withPathSensitivity(PathSensitivity.RELATIVE)
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
