plugins {
    kotlin("android") version "1.9.0"
    kotlin("android.extensions") version "1.9.0"
    kotlin("kapt") version "1.9.0"
    id("com.android.application") version "8.1.0" apply true
    id("com.google.devtools.kapt") version "1.9.0"
}

android {
    namespace "com.dynamic.dynamicbehavioradaptiveui"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.dynamic.dynamicbehavioradaptiveui"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    dependencies {
        implementation(platform("androidx.compose:compose-bom:2024.07.00"))
        implementation("androidx.compose.ui:ui")
        implementation("androidx.compose.ui:ui-graphics")
        implementation("androidx.compose.ui:ui-tooling-preview")
        implementation("androidx.compose.material3:material3")
        implementation("androidx.compose.material:material")
        implementation("androidx.lifecycle:lifecycle-runtime-ktx")
        implementation("androidx.activity:activity-compose")
        implementation("androidx.compose.animation:animation")
        implementation("androidx.compose.foundation:foundation")
        implementation("androidx.compose.foundation:foundation-layout")
        testImplementation("junit:junit:4.13.2")
        androidTestImplementation(platform("androidx.compose:compose-bom:2024.07.00"))
        androidTestImplementation("androidx.compose.ui:ui-test-junit4")
        androidTestImplementation("androidx.espresso:espresso-core:3.4.0")
    }
}

repositories {
    mavenCentral()
}