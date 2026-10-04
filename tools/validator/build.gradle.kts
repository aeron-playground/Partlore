plugins {
    id("partlore.jvm.library")
}

dependencies {
    implementation(libs.networknt.jsonschema)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.dataformat.yaml)
    // The schema validator logs through SLF4J; this receiver keeps the output quiet.
    runtimeOnly(libs.slf4j.nop)
}
