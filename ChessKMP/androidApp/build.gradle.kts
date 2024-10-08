plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    // https://github.com/google/ksp/releases?page=4
    id("com.google.devtools.ksp") version "1.9.20-1.0.13"
    //Kotlinx Serialization
    kotlin("plugin.serialization") version "1.9.20"
}

android {
    namespace = "com.dilip.chess_kmp.android"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.dilip.chess_kmp.android"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(projects.shared)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.androidx.activity.compose)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.core)
    ksp(libs.ksp)

    implementation(libs.androidx.core.splashscreen)

    implementation(libs.koin.androidx.compose)

    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(libs.accompanist.systemuicontroller)

    implementation(libs.coil.compose)
}