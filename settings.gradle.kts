pluginManagement {
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
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        exclusiveContent {
            forRepository {
                maven("https://oss.sonatype.org/content/repositories/snapshots") {
                    name = "Sonatype SNAPSHOTs"
                }
            }
            filter {
                includeVersionByRegex("com\\.google\\.dagger.*", ".*", "HEAD-SNAPSHOT")
            }
        }
    }
}

rootProject.name = "MyCurrencyConverter"
include(":app")
 