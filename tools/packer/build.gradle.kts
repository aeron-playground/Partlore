plugins {
    id("partlore.jvm.library")
    application
}

dependencies {
    implementation(project(":tools:validator"))
    implementation(project(":core:packformat"))
    implementation(libs.androidx.sqlite.bundled)
    implementation(libs.jackson.databind)
    testImplementation(testFixtures(project(":tools:validator")))
}

val schemaDir = rootProject.layout.projectDirectory.dir("content/schema")

tasks.withType<Test>().configureEach {
    systemProperty("partlore.schemaDir", schemaDir.asFile.absolutePath)
    inputs.dir(schemaDir).withPathSensitivity(PathSensitivity.RELATIVE)
    systemProperty(
        "partlore.contentDir",
        rootProject.layout.projectDirectory
            .dir("content")
            .asFile.absolutePath,
    )
    inputs.dir(rootProject.layout.projectDirectory.dir("content")).withPathSensitivity(PathSensitivity.RELATIVE)
}

application {
    mainClass = "dev.partlore.tools.packer.MainKt"
}

val contentDir = rootProject.layout.projectDirectory.dir("content")
val preview = providers.gradleProperty("preview").isPresent
val packDir = rootProject.layout.buildDirectory.dir(if (preview) "content/preview" else "content/pack")

val packContent =
    tasks.register<JavaExec>("packContent") {
        group = "build"
        description = "Builds the content pack and manifest.json (-Ppreview: a preview pack with drafts)."
        classpath = sourceSets.main.get().runtimeClasspath
        mainClass = application.mainClass
        workingDir = rootProject.projectDir
        args(
            "pack",
            "--content",
            "content",
            "--out",
            packDir
                .get()
                .asFile
                .relativeTo(rootProject.projectDir)
                .path,
        )
        if (preview) args("--preview")
        inputs.dir(contentDir).withPathSensitivity(PathSensitivity.RELATIVE)
        outputs.dir(packDir)
    }

tasks.named("check") { dependsOn(packContent) }

tasks.register<Test>("scaleTest") {
    group = "verification"
    description = "Validates and packs 5,000 generated parts and checks the speed targets (CI)."
    testClassesDirs =
        sourceSets.test
            .get()
            .output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    filter { includeTestsMatching("*ScaleTest") }
    systemProperty("partlore.scaleTest", "true")
    maxHeapSize = "1g"
    testLogging { showStandardStreams = true }
}

// Packs for the app: preview (drafts included) for debug builds, release (checked only) for release builds.
listOf("Preview" to true, "Release" to false).forEach { (name, preview) ->
    tasks.register<JavaExec>("pack${name}Asset") {
        group = "build"
        description = "Builds the ${name.lowercase()} pack the app bundles."
        classpath = sourceSets.main.get().runtimeClasspath
        mainClass = application.mainClass
        workingDir = rootProject.projectDir
        val out = layout.buildDirectory.dir("packs/${name.lowercase()}")
        args("pack", "--content", "content", "--out", out.get().asFile.path)
        if (preview) args("--preview")
        inputs.dir(contentDir).withPathSensitivity(PathSensitivity.RELATIVE)
        outputs.dir(out)
    }
}
