plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "2.0.0"
}

android {
    namespace = "com.bookyo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.bookyo"
        minSdk = 24
        targetSdk = 35
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
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")

    implementation("com.google.accompanist:accompanist-permissions:0.32.0")
    implementation("androidx.compose.ui:ui-text-google-fonts:1.5.4")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.material:material:1.5.0")

    implementation("io.coil-kt.coil3:coil-compose:3.1.0")

    // Amplify dependency
    implementation("com.amplifyframework:core-kotlin:2.24.0")
    implementation("com.amplifyframework:aws-api:2.24.0")
    implementation("com.amplifyframework:aws-auth-cognito:2.24.0")
    implementation("com.amplifyframework:aws-analytics-pinpoint:2.24.0")
    implementation("com.amplifyframework:core:2.24.0")
    implementation("com.amplifyframework:aws-core:2.24.0")
    implementation("com.amplifyframework:aws-storage-s3:2.24.0")

    // WorkManager for background tasks
    implementation("androidx.work:work-runtime-ktx:2.8.1")

    // DataStore for preference storage
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Kotlin Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("com.github.bumptech.glide:compose:1.0.0-beta01")


}