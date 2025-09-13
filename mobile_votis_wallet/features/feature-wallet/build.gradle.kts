import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "FeatureWallet"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Core modules dependencies
            implementation(projects.core.coreUi)
            implementation(projects.core.coreDomain)
            implementation(projects.core.coreCommon)

            // Compose dependencies
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            // Lifecycle & ViewModel
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // DateTime for domain models
            implementation(libs.kotlinx.datetime)

            // Charts
            implementation(libs.koalaplot.core)

            // FlexibleBottomSheet for advanced bottom sheet with edge-to-edge support
            implementation(libs.flexible.bottomsheet)

            // DI
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
        }

        androidMain.dependencies {
            implementation(compose.preview)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "finance.votis.wallet.feature.wallet"
    compileSdk =
        libs.versions.android.compileSdk
            .get()
            .toInt()

    defaultConfig {
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// Ktlint configuration
ktlint {
    version.set("1.3.1")
    android.set(true)
    ignoreFailures.set(false)
    filter {
        exclude("**/generated/**")
        exclude("**/build/**")
    }
}

// Detekt configuration
detekt {
    toolVersion = libs.versions.detekt.get()
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
}
