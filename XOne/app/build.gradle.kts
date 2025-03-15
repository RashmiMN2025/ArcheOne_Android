plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.archeGlobal.one"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.archeGlobal.one"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            abiFilters += listOf("armeabi", "armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file("/Users/netcon/Documents/AndroidSignedIn/your_keystore.jks") // Update the keystore name
            storePassword = "Android@12345"
            keyAlias = "key0"
            keyPassword = "Android@12345"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isCrunchPngs = true
            proguardFiles += file("proguard-rules-r8.pro")
            signingConfig = signingConfigs.getByName("release")
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.core.splashscreen)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.navigation.compose)
    implementation (libs.coil)
    implementation (libs.coil.compose)
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.gson)
    implementation(libs.okhttp) // Latest stable version
    implementation (libs.ui)
    implementation (libs.material3)
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.okhttp.v490)
    implementation (libs.androidx.ui.v130)
    implementation (libs.androidx.material3.v101)
    implementation (libs.androidx.ui.v140) // or latest version
    implementation (libs.androidx.material3.v110) // Ensure you have material3
    implementation (libs.androidx.foundation) // or latest version
    implementation (libs.androidx.material) // for Material components
    implementation (libs.androidx.runtime.livedata)
    implementation (libs.androidx.foundation.vlatestversion)
    implementation (libs.androidx.ui.vlatestversion)
    implementation (libs.androidx.ui.v150)
    implementation (libs.androidx.material3.v120)
    implementation (libs.androidx.foundation.v150)
    implementation(libs.logging.interceptor)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation (libs.androidx.core.ktx.v1120)
    implementation (libs.androidx.appcompat)
    implementation (libs.material)  // Material 3
    implementation (libs.accompanist.pager)
}