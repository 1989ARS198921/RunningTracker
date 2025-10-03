// File: app/build.gradle.kts
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // --- ДОБАВЛЕНО: плагин для kapt (Kotlin Annotation Processing Tool) ---
    id("kotlin-kapt") // <-- Эта строка добавлена
    // --- КОНЕЦ ДОБАВЛЕНИЯ ---
}

android {
    namespace = "com.example.runningtracker"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.runningtracker"
        minSdk = 31
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
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // --- ДОБАВЛЕНО: зависимости для Room ---
    val roomVersion = "2.6.1" // Рекомендуется использовать последнюю стабильную версию

    implementation("androidx.room:room-runtime:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion") // Компилятор для Room
    // --- КОНЕЦ ДОБАВЛЕНИЯ ---
}