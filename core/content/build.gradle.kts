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
