plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "bg.zanaiti.craftguide"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "bg.zanaiti.craftguide"
        minSdk = 24
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
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Networking (Retrofit + Moshi)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Moshi for JSON parsing
    implementation("com.squareup.moshi:moshi:1.15.1")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")

    // Image loading
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("com.google.android.exoplayer:exoplayer:2.19.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation("androidx.navigation:navigation-compose:2.8.9")

    // Open Street Map
    implementation("org.osmdroid:osmdroid-android:6.1.20")
    implementation("androidx.compose.ui:ui-viewbinding:1.7.8")

    // Navigation (down)
    implementation("androidx.navigation:navigation-compose:2.8.9")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation(libs.androidx.material3)

    // location
    implementation("com.google.accompanist:accompanist-permissions:0.35.0-alpha")

    // DataStore за запазване на токен
    implementation("androidx.datastore:datastore-preferences:1.1.0")

    // Security (за безопасно съхранение)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}