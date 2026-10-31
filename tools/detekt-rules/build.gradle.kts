import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    id("partlore.jvm.library")
}

// These rules run inside detekt 1.23, which runs Kotlin 2.0: they may only use the 2.0 standard library.
// Kotlin warns that API version 2.0 is old; that is the point here, so only that warning is turned off.
kotlin {
    compilerOptions {
        apiVersion.set(KotlinVersion.KOTLIN_2_0)
        freeCompilerArgs.add("-Xsuppress-version-warnings")
    }
}

dependencies {
    compileOnly(libs.detekt.api)
    testImplementation(libs.detekt.api)
    testImplementation(libs.detekt.test)
}
