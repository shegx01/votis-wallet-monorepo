plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
    id("com.github.ben-manes.versions") version "0.51.0"
}

// Define common quality tasks that run across all modules
tasks.register("formatCode") {
    description = "Format all Kotlin code using ktlint"
    group = "formatting"
    dependsOn(subprojects.map { ":${it.name}:ktlintFormat" })
}

tasks.register("checkCodeStyle") {
    description = "Check code style using ktlint"
    group = "verification"
    dependsOn(subprojects.map { ":${it.name}:ktlintCheck" })
}

tasks.register("codeQuality") {
    description = "Run all code quality checks (ktlint + detekt)"
    group = "verification"
    dependsOn("checkCodeStyle")
    dependsOn(subprojects.map { ":${it.name}:detekt" })
}

tasks.register("formatAndCheck") {
    description = "Format code and run quality checks"
    group = "verification"
    dependsOn("formatCode")
    finalizedBy("codeQuality")
}

tasks.register("testAll") {
    description = "Run tests for all modules"
    group = "verification"
    dependsOn(subprojects.map { ":${it.name}:test" })
}

tasks.register("buildAll") {
    description = "Build all modules"
    group = "build"
    dependsOn(subprojects.map { ":${it.name}:build" })
}
