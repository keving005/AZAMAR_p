plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
    alias(libs.plugins.compose.compiler)
    id("com.google.gms.google-services")
    // SE ELIMINÓ LA LÍNEA QUE CAUSABA EL ERROR (plugin.compose)
}

android {
    namespace = "com.example.proyect"
    compileSdk = 34 // Versión estable y compatible

    defaultConfig {
        applicationId = "com.example.proyect"
        minSdk = 26 // Android 8.0 (Compatible con casi todo el mundo)
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }


}

dependencies {
    // --- BOMs (Manejo automático de versiones) ---
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation(platform("com.google.firebase:firebase-bom:32.7.2"))

    // --- Android Core & UI ---
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")

    // --- Compose & Navigation ---
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.runtime:runtime-livedata")

    // --- Firebase ---
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")

    // --- Google Maps & Redes ---
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.android.volley:volley:1.2.1")

    // --- OTROS ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // --- LIBRERÍA DEL CALENDARIO (WEEK VIEW) ---

}