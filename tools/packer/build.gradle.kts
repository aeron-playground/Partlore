plugins {
    id("partlore.jvm.library")
}

dependencies {
    implementation(project(":tools:validator"))
    implementation(project(":core:packformat"))
    implementation(libs.androidx.sqlite.bundled)
    testImplementation(testFixtures(project(":tools:validator")))
}

val schemaDir = rootProject.layout.projectDirectory.dir("content/schema")

tasks.withType<Test>().configureEach {
    systemProperty("partlore.schemaDir", schemaDir.asFile.absolutePath)
    inputs.dir(schemaDir).withPathSensitivity(PathSensitivity.RELATIVE)
}
