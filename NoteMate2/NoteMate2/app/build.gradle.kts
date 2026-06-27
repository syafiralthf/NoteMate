plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.notemate"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.notemate"
        minSdk = 23 // Sesuai permintaan sebelumnya untuk menghindari error manifest merger
        targetSdk = 36
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
}

dependencies {
    // --- ROOM DATABASE ---
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")

    // --- GOOGLE GEMINI AI ---
    // Library utama untuk fitur AI Summarizer
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    // Diperlukan untuk menangani ListenableFuture (Callback) di Java
    implementation("com.google.guava:guava:31.0.1-android")

    // --- UI & ANDROIDX ---
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // --- TESTING ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}