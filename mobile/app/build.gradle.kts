import com.android.build.api.dsl.AaptOptions

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
        minSdk = 25
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    aaptOptions {
        noCompress("tflite")
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

    sourceSets {
        getByName("main") {
            assets.srcDirs("src/main/assets", "src/main/ml")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        mlModelBinding = false
    }

    packaging {
        resources {
            // SceneView/ARCore специфични настройки
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.0")

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
    implementation("com.github.MKergall:osmbonuspack:6.9.0")

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
    implementation(libs.compose.material3)

    // --- AR & 3D Dependencies ---
    // SceneView за Compose (базиран на ARCore)
    implementation("io.github.sceneview:arsceneview:2.2.1")
    // Google ARCore Core Library
    implementation("com.google.ar:core:1.41.0")
    // Допълнителни инструменти за работа с изображения (ако се наложи)
    implementation("androidx.core:core-ktx:1.12.0")

    // ML Kit Object Detection (без ARCore)
    implementation("com.google.mlkit:object-detection:17.0.2")
    implementation("com.google.mlkit:object-detection-custom:17.0.2")

    // CameraX
    implementation("androidx.camera:camera-core:1.4.2")
    implementation("androidx.camera:camera-camera2:1.4.2")
    implementation("androidx.camera:camera-lifecycle:1.4.2")
    implementation("androidx.camera:camera-view:1.4.2")

    implementation("com.google.mlkit:image-labeling:17.0.7")
    implementation("com.google.mlkit:image-labeling-custom:17.0.1")
    implementation("com.google.android.gms:play-services-mlkit-image-labeling:16.0.8")
    //implementation("com.google.ar:core:1.33.0")
    //implementation("com.google.ar.sceneform.ux:sceneform-ux:1.17.1")

    // TensorFlow Lite библиотеки (за да спре да дава грешка в Model.java)
//    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
//    implementation("org.tensorflow:tensorflow-lite-metadata:0.4.4")
//    implementation("org.tensorflow:tensorflow-lite:2.14.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}