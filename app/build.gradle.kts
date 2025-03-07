plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.hamza.blogapp_custombackend"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.hamza.blogapp_custombackend"
        minSdk = 24
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.okhttp)
    implementation(libs.java.jwt)
    implementation(libs.okhttp.sse)
    implementation(libs.material.v140)
    implementation (libs.material)
    implementation(libs.lombok)
    annotationProcessor(libs.lombok)
    implementation(libs.gson)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
