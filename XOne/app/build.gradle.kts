plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.xone"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.xone"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
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
    // Ensure this is included in your build.gradle file
    implementation (libs.androidx.foundation.vlatestversion)
    implementation (libs.androidx.ui.vlatestversion)
    implementation (libs.androidx.ui.v150)
    implementation (libs.androidx.material3.v120)
    implementation (libs.androidx.foundation.v150)
}