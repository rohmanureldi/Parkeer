pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://jfrog.cicd.telkomsel.co.id/artifactory/mytelkomsel-maven-local")
            credentials {
                username = System.getenv("JFROG_USERNAME") ?: ""
                password = System.getenv("JFROG_PASSWORD") ?: ""
            }
        }

    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://jfrog.cicd.telkomsel.co.id/artifactory/mytelkomsel-maven-local")
            credentials {
                username = System.getenv("JFROG_USERNAME") ?: ""
                password = System.getenv("JFROG_PASSWORD") ?: ""
            }
        }
        maven("https://artifacts.netcore.co.in/artifactory/android")
        maven("https://jitpack.io")
    }
}

rootProject.name = "Parkeer"

include(":app")
include(":core:model")
include(":core:nfc")
include(":core:crypto")
include(":core:cardprotocol")
include(":core:ui")
include(":feature:station")
include(":feature:gate")
include(":feature:terminal")
include(":feature:scout")
