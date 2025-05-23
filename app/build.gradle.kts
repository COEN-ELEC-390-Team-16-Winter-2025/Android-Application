plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.drinkwise.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.drinkwise.app"
        minSdk = 29
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:32.8.0")) // Firebase BOM version

    implementation("com.google.firebase:firebase-auth") // Version managed by BOM
    implementation("com.google.firebase:firebase-firestore") // Version managed by BOM
    implementation("com.google.firebase:firebase-database") // Version managed by BOM
    implementation("com.google.firebase:firebase-messaging") // Version managed by BOM
    implementation("com.google.firebase:firebase-storage")

    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation(libs.locationdelegation)
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")


    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")

    //dependencies needed for google map
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.maps.android:android-maps-utils:3.4.0")
    implementation("com.android.volley:volley:1.2.1")



    // Other dependencies
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.activity)

    // Testing libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}



