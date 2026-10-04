plugins {
    id("partlore.jvm.library")
    `java-test-fixtures`
    application
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

application {
    mainClass = "dev.partlore.tools.content.cli.MainKt"
}

val contentDir = rootProject.layout.projectDirectory.dir("content")
val validatedStamp = layout.buildDirectory.file("validate-content.ok")

// Runs from the repository root so problems print as content/parts/…, which GitHub links to the PR.
val validateContent =
    tasks.register<JavaExec>("validateContent") {
        group = "verification"
        description = "Checks every part in content/ (-Ppreview: drafts may ship)."
        classpath = sourceSets.main.get().runtimeClasspath
        mainClass = application.mainClass
        workingDir = rootProject.projectDir
        args("validate", "--content", "content")
        if (providers.gradleProperty("preview").isPresent) args("--preview")
        inputs.dir(contentDir).withPathSensitivity(PathSensitivity.RELATIVE)
        // A local copy: the configuration cache can't store references to the build script itself.
        val stamp = validatedStamp
        outputs.file(stamp)
        doLast { stamp.get().asFile.writeText("ok\n") }
    }

tasks.register<JavaExec>("contentChecklist") {
    group = "documentation"
    description = "Writes a page-grouped checklist per part to build/content/checklists (-Ppart=<maker/part>)."
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = application.mainClass
    workingDir = rootProject.projectDir
    args("checklist", "--content", "content", "--out", "build/content/checklists")
    providers.gradleProperty("part").orNull?.let { args("--part", it) }
}

tasks.named("check") { dependsOn(validateContent) }
