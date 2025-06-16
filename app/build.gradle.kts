plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.lunasin"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.lunasin"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }

    sourceSets {
        getByName("main") {
            java.srcDirs("src/kotlin+java")
            res.srcDirs("src/res")
            manifest.srcFile("src/main/AndroidManifest.xml")
        }
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // --- Firebase Core Dependencies ---
    implementation(platform("com.google.firebase:firebase-bom:33.10.0"))
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-analytics")
    implementation(libs.firebase.storage.ktx)
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // --- Jetpack Compose BOM ---
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // --- Compose UI, Foundation, Material3 ---
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")

    // --- Compose Tooling (Preview) ---
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // --- Compose Icon Libraries ---
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")

    // --- Compose Integration with Activities & ViewModels ---
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.5")

    // --- LiveData Integration with Compose ---
    implementation("androidx.compose.runtime:runtime-livedata")

    // --- Kotlin Coroutines ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // --- Compose Navigation ---
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // --- Coil (Image Loading for Compose) ---
    implementation("io.coil-kt:coil-compose:2.6.0")

    // --- Accompanist Pager (Jika masih digunakan) ---
    implementation("com.google.accompanist:accompanist-pager:0.30.1")

    // --- Kalender (Kizito Wose Calendar) ---
    implementation("com.kizitonwose.calendar:compose:2.5.1")

    // --- QR Code ---
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // --- Android KTX Core ---
    implementation(libs.androidx.core.ktx)

    // --- Material Components untuk Tema XML ---
    // INI YANG DITAMBAHKAN untuk mengatasi error "Theme.Material3... not found" di XML
    implementation("com.google.android.material:material:1.12.0")

    // --- Test Dependencies ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}