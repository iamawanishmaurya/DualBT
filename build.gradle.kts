buildscript {
    if (System.getenv("DUALBT_PLANNED_STACK") == "true") {
        repositories {
            val offlineMaven = System.getenv("DUALBT_OFFLINE_MAVEN")
            if (!offlineMaven.isNullOrBlank()) {
                maven { url = uri(offlineMaven) }
            }
            google()
            mavenCentral()
            gradlePluginPortal()
        }
        dependencies {
            classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22")
            classpath("com.google.dagger:hilt-android-gradle-plugin:2.51")
        }
    }
}

plugins {
    id("com.android.application") version "8.2.2" apply false
}
