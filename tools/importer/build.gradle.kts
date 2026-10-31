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
    // The golden test compares with a checked board in content/, and reads the guide from a local
    // esp-dev-kits checkout given with -PespDevKits=<path> (skipped without it: no network in CI).
    val contentDir = rootProject.layout.projectDirectory.dir("content")
    systemProperty("partlore.contentDir", contentDir.asFile.absolutePath)
    inputs.dir(contentDir).withPathSensitivity(PathSensitivity.RELATIVE)
    providers.gradleProperty("espDevKits").orNull?.let {
        systemProperty("espDevKits", it)
        inputs.property("espDevKits", it)
    }
}

application {
    mainClass = "dev.partlore.tools.importer.MainKt"
}

// ./gradlew :tools:importer:importEspDevKits -Pcheckout=<esp-dev-kits checkout> -Pboard=<hints file name>
tasks.register<JavaExec>("importEspDevKits") {
    group = "content"
    description = "Imports a board's pins from a local esp-dev-kits checkout and its hints file."
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = application.mainClass
    workingDir = rootProject.projectDir
    val checkout = providers.gradleProperty("checkout")
    val board = providers.gradleProperty("board")
    argumentProviders.add(
        CommandLineArgumentProvider {
            val path = checkout.orNull ?: error("set -Pcheckout=<path to an esp-dev-kits checkout>")
            val name = board.orNull ?: error("set -Pboard=<hints file name, for example esp32-s3-devkitc-1>")
            listOf("--checkout", path, "--hints", "content/import/esp-dev-kits/$name.yaml", "--out", "content/parts")
        },
    )
}
