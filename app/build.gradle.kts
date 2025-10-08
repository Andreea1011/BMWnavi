plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    kotlin("kapt")
    id("org.jetbrains.kotlin.plugin.compose")

}

android {
    namespace = "com.example.bmwnavi"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.bmwnavi"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "0.1"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures { compose = true }
    kotlinOptions { jvmTarget = "17" }

    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.material3)
    implementation(libs.ui)
    implementation(libs.ui.tooling.preview)
    debugImplementation(libs.ui.tooling)

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    // Hilt DI

    implementation(libs.androidx.material.icons.extended)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    // Coroutines (handy later)
    implementation(libs.kotlinx.coroutines.android)

    // --- Networking ---
    implementation(libs.retrofit)
    //noinspection NewerVersionAvailable
    implementation(libs.converter.moshi)
    implementation("com.squareup.okhttp3:okhttp:5.2.0")
    implementation(libs.logging.interceptor)

// --- Moshi (JSON parser used by converter-moshi) ---
    implementation(libs.moshi)
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.15.2")
}
