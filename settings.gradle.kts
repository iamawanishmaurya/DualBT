pluginManagement {
    repositories {
        val offlineMaven = System.getenv("DUALBT_OFFLINE_MAVEN")
        if (!offlineMaven.isNullOrBlank()) {
            maven { url = uri(offlineMaven) }
        }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        val offlineMaven = System.getenv("DUALBT_OFFLINE_MAVEN")
        if (!offlineMaven.isNullOrBlank()) {
            maven { url = uri(offlineMaven) }
        }
        google()
        mavenCentral()
    }
}
rootProject.name = "DualBT"
include(":app")
