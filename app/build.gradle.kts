plugins {
    alias(libs.plugins.android.application)

    alias(libs.plugins.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
}

android {
    buildFeatures{
        viewBinding = true
        dataBinding = true
    }
    namespace = "com.example.daltutor"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.daltutor"
        minSdk = 34
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
    }
    packagingOptions {
        exclude("META-INF/*")
    }
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.database)
    implementation(libs.firebase.auth)

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.crashlytics)
    implementation(libs.uiautomator)

    // JavaMail API Dependencies for Sending Email
    implementation("com.sun.mail:android-mail:1.6.2")
    implementation("com.sun.mail:android-activation:1.6.2")

    // Google Maps-related dependencies
    implementation(libs.googlePlayServicesMaps)
    implementation(libs.googlePlayServicesLocation)
    implementation(libs.androidMapsUtils)
    implementation(libs.androidx.espresso.intents)

    testImplementation(libs.junit)
    testImplementation(libs.monitor)
    testImplementation(libs.core)
    testImplementation(libs.ext.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.androidx.test.rules)

    implementation(libs.paypalSdk)

    implementation("androidx.compose.material3:material3:1.1.2") // For Compose
    implementation("com.google.android.material:material:1.11.0") // For XML-based Material 3

    //auth library
    implementation(libs.firebase.auth)
    implementation("com.google.firebase:firebase-messaging:24.1.1")
    implementation("com.google.firebase:firebase-bom:33.11.0")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.23.0")
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.google.firebase:firebase-messaging:24.1.1")
}





apply(plugin = "com.google.gms.google-services")