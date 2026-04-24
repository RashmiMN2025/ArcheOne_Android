plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // id("org.jlleitschuh.gradle.ktlint") version "13.1.0"
}

android {

    namespace = "com.archeGlobal.one"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.archeGlobal.one"
        minSdk = 26
        targetSdk = 35
        versionCode = 25
        versionName = "1.9"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            abiFilters += listOf("armeabi", "armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            isCrunchPngs = true
            proguardFiles += file("proguard-rules-r8.pro")
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    lint {
        abortOnError = true
        warningsAsErrors = false
    }
}

dependencies {
    implementation(libs.androidx.material3.window.size)
    // AndroidX Core and Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.appcompat)

    // Compose
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3.v132)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.foundation)

    // Manual implementation of UI graphics to ensure it's included
    implementation(libs.ui.graphics)

    // Material Design
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.material)
    // Compose Material (Material Design 2) for components like Card, Scaffold, etc.
    implementation(libs.androidx.material)

    // Splash Screen
    implementation(libs.androidx.core.splashscreen)

    // Image Loading
    implementation(libs.coil)
    implementation(libs.coil.compose)

    // Network
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp.v4110)
    implementation(libs.logging.interceptor.v4110)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)

    // Accompanist
    implementation(libs.accompanist.pager)
    implementation(libs.accompanist.swiperefresh)
    implementation(libs.lottie.compose)

    // PDF Viewer - use web view based approach instead of PDF library
    implementation(libs.androidx.webkit)
    implementation(libs.generativeai)

    // biometric
    implementation(libs.androidx.biometric)

    // ZXing QR Code generator
    implementation(libs.core)
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.protolite.well.known.types)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui.unit)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.ui.text)
    implementation(libs.androidx.compose.material3)
    implementation(libs.foundation)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.room.ktx)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // QR Scanner
    implementation("androidx.camera:camera-core:1.5.1")
    implementation("androidx.camera:camera-camera2:1.5.1")
    implementation("androidx.camera:camera-lifecycle:1.5.1")
    implementation("androidx.camera:camera-view:1.5.1")
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    implementation("com.squareup.retrofit2:retrofit:2.x.x")
    implementation("com.squareup.retrofit2:converter-gson:2.x.x")

    implementation("androidx.compose.ui:ui-tooling-preview:1.9.3")
}
