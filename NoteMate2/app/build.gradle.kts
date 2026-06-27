plugins {
    alias(libs.plugins.android.application)
    // Plugin Kotlin diperlukan agar library Gemini AI dan blok kotlinOptions berfungsi
    id("org.jetbrains.kotlin.android") version "1.9.0"
}

android {
    namespace = "com.example.notemate"
    compileSdk = 36

    // Disetel ke 34 agar stabil (Menghindari error redirect.txt pada SDK 36)

    defaultConfig {
        applicationId = "com.example.notemate"
        minSdk = 23
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
        // Menggunakan Java 11 untuk mendukung library modern (Gemini, Room, & Biometric)
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // Konfigurasi ini diperlukan karena library Google AI menggunakan Kotlin
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // --- ROOM DATABASE (Penyimpanan Catatan) ---
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")

    // --- GOOGLE GEMINI AI (Fitur Summarizer) ---
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    implementation("com.google.guava:guava:31.0.1-android")

    // --- BIOMETRIC LOCK (Fitur Keamanan Baru) ---
    // Library ini menangani Scan Wajah dan Sidik Jari secara otomatis
    implementation("androidx.biometric:biometric:1.1.0")

    // --- UI & ANDROIDX LIBRARIES ---
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // --- UNIT TESTING ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}