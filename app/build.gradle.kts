plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    // Pastikan versi plugin ini sesuai, ini harusnya sama dengan kotlinCompilerExtensionVersion
    // atau gunakan versi plugin yang direkomendasikan Compose, misal "1.5.1"
    id("org.jetbrains.kotlin.plugin.compose") // HAPUS 'version "2.0.0"' jika plugin ini datang dari root project build.gradle.kts
}

android {
    namespace = "com.example.lunasin"
    compileSdk = 35 // Tetap di 35

    buildFeatures {
        compose = true
        // viewBinding = true // Hapus jika Anda 100% menggunakan Compose UI dan tidak ada lagi XML View Binding
    }


    composeOptions {
        // PERHATIKAN INI: Versi kotlinCompilerExtensionVersion harus kompatibel dengan Compose BOM.
        // Untuk compose-bom:2025.02.00, compiler extension 1.8.0 sangat mungkin tidak kompatibel.
        // Coba gunakan versi yang lebih baru, misal 1.5.12 atau 1.5.13 yang cocok untuk Compose 1.6.x (versi Compose 1.6.x kompatibel dengan BOM 2025.02.00)
        // Atau biarkan kosong jika Anda menggunakan plugin compose versi terbaru dan defaultnya cocok.
        kotlinCompilerExtensionVersion = "1.5.12" // Ganti dengan versi yang kompatibel atau yang paling baru yang direkomendasikan
    }

    defaultConfig {
        applicationId = "com.example.lunasin"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true // Penting untuk Vector Assets
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
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    // --- PASTIkan BLOK sourceSets INI ADA DAN SESUAI PATH ANDA ---
    sourceSets {
        getByName("main") {
            java.srcDirs("src/kotlin+java") // Lokasi folder kode sumber Anda
            res.srcDirs("src/res")         // Lokasi folder resource Anda
            manifest.srcFile("src/main/AndroidManifest.xml") // Lokasi AndroidManifest.xml Anda
        }
    }
    // -----------------------------------------------------
}

dependencies {
    // --- Firebase Core Dependencies ---
    implementation(platform("com.google.firebase:firebase-bom:33.10.0")) // Firebase Bill of Materials
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-analytics")
    implementation(libs.firebase.storage.ktx) // Dari libs.versions.toml
    implementation("com.google.android.gms:play-services-auth:20.7.0") // Google Sign-In

    // --- Jetpack Compose BOM ---
    // Gunakan Compose BOM untuk mengelola versi dependencies Compose
    val composeBom = platform("androidx.compose:compose-bom:2025.02.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // --- Compose UI, Foundation, Material3 ---
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-text") // Untuk komponen teks yang lebih spesifik jika diperlukan
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3") // Material Design 3

    // --- Compose Tooling (Preview) ---
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest") // Untuk UI Tests debug

    // --- Compose Icon Libraries ---
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")

    // --- Compose Integration with Activities & ViewModels ---
    implementation("androidx.activity:activity-compose:1.10.0") // Integrate Compose with Activity
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.5") // ViewModel for Compose

    // --- LiveData Integration with Compose Runtime ---
    // Ini penting untuk menggunakan .observeAsState() pada LiveData
    implementation("androidx.compose.runtime:runtime-livedata:1.6.8") // Anda sudah menambahkan ini, bagus

    // --- Kotlin Coroutines ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1") // Pastikan versi Android juga

    // --- Compose Navigation ---
    implementation("androidx.navigation:navigation-compose:2.7.4")

    // --- Hapus MPAndroidChart karena kita akan pakai Compose Canvas/Library Compose Chart ---
    // implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0") // BARIS INI DIHAPUS

    // --- Coil (Image Loading for Compose) ---
    implementation ("io.coil-kt:coil-compose:2.4.0")

    // --- Accompanist Pager (Jika masih digunakan) ---
    implementation("com.google.accompanist:accompanist-pager:0.30.1")

    // --- Kalender Update (Kizito Wose Calendar) ---
    implementation("com.kizitonwose.calendar:compose:2.3.0")

    // --- QR Code ---
    implementation ("com.google.zxing:core:3.5.1")
    implementation ("androidx.core:core-ktx:1.10.1") // Ini sudah cukup standar, mungkin tidak perlu QR library tambahan jika hanya core
    implementation ("com.journeyapps:zxing-android-embedded:4.3.0") // Jika ini untuk View-based QR scanner, mungkin akan butuh AndroidView di Compose

    // --- Dependencies Umum yang mungkin tidak perlu lagi jika 100% Compose UI ---
    // implementation(libs.androidx.appcompat) // Hanya jika masih ada AppCompatActivity
    // implementation(libs.material) // Hanya jika masih ada Material Components for Android Views
    implementation(libs.androidx.core.ktx) // Ini masih standar dan baik

    // --- Test Dependencies ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4") // Untuk UI testing Compose
}