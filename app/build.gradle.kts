plugins {
    id("com.android.application")
}

val plannedStackEnabled = providers.environmentVariable("DUALBT_PLANNED_STACK")
    .map { it.equals("true", ignoreCase = true) }
    .orElse(false)
    .get()

if (plannedStackEnabled) {
    pluginManager.apply("org.jetbrains.kotlin.android")
    pluginManager.apply("com.google.dagger.hilt.android")
    pluginManager.apply("kotlin-kapt")
}

android {
    namespace = "com.xpwnit.dualbt"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.xpwnit.dualbt"
        minSdk = 29
        targetSdk = 34
        versionCode = 39
        versionName = "0.2.38"

        if (plannedStackEnabled) {
            externalNativeBuild {
                cmake {
                    cppFlags("-std=c++17")
                    arguments("-DANDROID_STL=c++_shared")
                }
            }
            ndk {
                abiFilters += listOf("arm64-v8a", "x86_64")
            }
        }
    }

    if (plannedStackEnabled) {
        ndkVersion = "27.1.12297006"

        externalNativeBuild {
            cmake {
                path = file("src/main/cpp/CMakeLists.txt")
                version = "3.22.1"
            }
        }

        buildFeatures {
            compose = true
            prefab = true
        }
        composeOptions {
            kotlinCompilerExtensionVersion = "1.5.8"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file(System.getenv("DUALBT_DEBUG_KEYSTORE") ?: "/tmp/dualbt-debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }
}

if (plannedStackEnabled) {
    tasks.withType<JavaCompile>().configureEach {
        exclude(
            "com/xpwnit/dualbt/DualBTApp.java",
            "com/xpwnit/dualbt/MainActivity.java",
            "com/xpwnit/dualbt/logging/AppLogger.java",
            "com/xpwnit/dualbt/logging/LogStore.java",
            "com/xpwnit/dualbt/service/DualBTService.java"
        )
    }
}

dependencies {
    if (plannedStackEnabled) {
        implementation(platform("androidx.compose:compose-bom:2024.02.00"))
        implementation("androidx.compose.ui:ui")
        implementation("androidx.compose.material3:material3")
        implementation("androidx.compose.material:material-icons-extended")
        implementation("androidx.compose.ui:ui-tooling-preview")
        implementation("androidx.compose.animation:animation")
        implementation("androidx.activity:activity-compose:1.8.2")

        implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
        implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
        implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

        implementation("com.google.dagger:hilt-android:2.51")
        add("kapt", "com.google.dagger:hilt-android-compiler:2.51")
        implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
        implementation("com.google.oboe:oboe:1.8.0")

        testImplementation("junit:junit:4.13.2")
        androidTestImplementation("androidx.test.ext:junit:1.1.5")
        androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
        testImplementation("org.mockito:mockito-core:5.8.0")
        testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

        debugImplementation("androidx.compose.ui:ui-tooling")
    }
}
