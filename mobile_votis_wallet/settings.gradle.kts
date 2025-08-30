rootProject.name = "mobileVotisWallet"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":composeApp")

// Core modules
include(":core:core-common")
include(":core:core-ui")
include(":core:core-domain")
include(":core:core-data")
include(":core:core-network")
include(":core:core-di")

// Feature modules
include(":features:feature-auth")
include(":features:feature-onboarding")
include(":features:feature-wallet")
include(":features:feature-transactions")
include(":features:feature-settings")
