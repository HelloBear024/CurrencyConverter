plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.hilt.android.gradle.plugin)
    id("kotlin-kapt");
}

android {
    namespace = "com.currecy.mycurrencyconverter"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.currecy.mycurrencyconverter"
        minSdk = 29
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation (libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.androidx.work.runtime.ktx)
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.navigation:navigation-compose:2.7.4")
    implementation(libs.androidx.espresso.core)


    implementation ("com.squareup:javapoet:1.13.0")

    implementation (libs.hilt.android)
    implementation(libs.androidx.hilt.common)
    kapt(libs.hilt.compiler)
    implementation (libs.androidx.hilt.navigation.compose)
//    implementation (libs.androidx.hilt.lifecycle.viewmodel)

//    implementation(libs.androidx.wear.complications.data)
    implementation(libs.androidx.hilt.work)
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.camera:camera-core:1.1.0")
    implementation("androidx.camera:camera-camera2:1.1.0")
    implementation("androidx.camera:camera-lifecycle:1.1.0")
    implementation("androidx.camera:camera-view:1.0.0-alpha31")
    implementation ("com.google.mlkit:text-recognition:16.0.0")
    debugImplementation ("androidx.room:room-testing:2.4.2")
    implementation("io.coil-kt:coil-compose:2.1.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.0")
    implementation ("com.google.accompanist:accompanist-permissions:0.31.1-alpha")

    implementation ("com.patrykandpatrick.vico:core:2.0.0-beta.1")
    implementation("com.patrykandpatrick.vico:compose:2.0.0-beta.1")
    implementation("com.patrykandpatrick.vico:compose-m3:2.0.0-beta.1")
    implementation("com.patrykandpatrick.vico:views:2.0.0-beta.1")


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}