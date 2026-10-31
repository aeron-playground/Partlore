plugins {
    id("partlore.jvm.library")
    application
}

dependencies {
    implementation(project(":tools:validator"))
    implementation(libs.jackson.databind)
    implementation(libs.jackson.dataformat.yaml)
    testImplementation(testFixtures(project(":tools:validator")))
}

val schemaDir = rootProject.layout.projectDirectory.dir("content/schema")

tasks.withType<Test>().configureEach {
    // Imported files are checked against the real schema files and rules.
    systemProperty("partlore.schemaDir", schemaDir.asFile.absolutePath)
    inputs.dir(schemaDir).withPathSensitivity(PathSensitivity.RELATIVE)
}

application {
    mainClass = "dev.partlore.tools.importer.MainKt"
}
