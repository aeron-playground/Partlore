pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Partlore"

include(":app")
include(":core:designsystem")
include(":core:model")
include(":core:content")
include(":core:packformat")
include(":core:testing")
include(":core:userdata")
include(":feature:onboarding")
include(":feature:settings")
include(":tools:packer")
include(":tools:validator")
