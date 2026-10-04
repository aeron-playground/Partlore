plugins {
    id("partlore.jvm.library")
    `java-test-fixtures`
}

dependencies {
    implementation(libs.networknt.jsonschema)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.dataformat.yaml)
    // The schema validator logs through SLF4J; this receiver keeps the output quiet.
    runtimeOnly(libs.slf4j.nop)
}

val schemaDir = rootProject.layout.projectDirectory.dir("content/schema")

tasks.withType<Test>().configureEach {
    // Tests use the real schema files, so the schema and the code can't drift apart.
    systemProperty("partlore.schemaDir", schemaDir.asFile.absolutePath)
    inputs.dir(schemaDir).withPathSensitivity(PathSensitivity.RELATIVE)
}
