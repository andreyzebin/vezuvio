/*
 * This file was generated by the Gradle 'init' task.
 *
 * The settings file is used to specify which projects to include in your build.
 * For more detailed information on multi-project builds, please refer to https://docs.gradle.org/8.13/userguide/multi_project_builds.html in the Gradle documentation.
 */
pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://jitpack.io")
        gradlePluginPortal()
    }
}

// use only for home/dev use - e.q.: no enterprise repositories configuration
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        mavenCentral()
        maven(url = "https://jitpack.io")
        gradlePluginPortal()

    }
}



rootProject.name = "vezuvio"
include("app")
