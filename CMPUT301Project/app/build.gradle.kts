plugins {
    alias(libs.plugins.android.application)
    id("androidx.navigation.safeargs")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.cmput301project"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.cmput301project"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        vectorDrawables.useSupportLibrary = true

//        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunner = "com.example.cmput301project.MyTestRunner"
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
    buildFeatures {
        viewBinding = true
    }
    packagingOptions {
        resources {
            // Exclude duplicate MockMaker files
            excludes += "mockito-extensions/org.mockito.plugins.MockMaker"
        }
    }
}

dependencies {
//    implementation(files("C:/Users/fxj/AppData/Local/Android/Sdk/platforms/android-34/android.jar"))
    implementation(platform("com.google.firebase:firebase-bom:32.7.1"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage:20.0.0")

    implementation("androidx.activity:activity-ktx:1.2.0")    // For Activities
    implementation("androidx.fragment:fragment-ktx:1.3.0")   // For Fragments
    implementation("com.google.zxing:core:3.3.0")
    implementation("com.journeyapps:zxing-android-embedded:4.2.0")  // For Android integration
    implementation("com.github.bumptech.glide:glide:4.12.0")
//    implementation("com.google.android.material:material:1.11.0")
    implementation("com.google.android.material:material:1.0.0-beta01")
    implementation ("androidx.recyclerview:recyclerview:1.2.1")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(libs.runner)

    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    // Hilt and Dagger dependencies for Android
    implementation ("com.google.dagger:hilt-android:2.28-alpha")
    androidTestImplementation ("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.5.0")

    implementation("com.google.android.gms:play-services-tasks:17.2.1")
    // Mockito
    testImplementation ("org.mockito:mockito-core:3.11.2")
    androidTestImplementation ("org.mockito:mockito-android:3.11.2")
    androidTestImplementation ("org.mockito:mockito-inline:3.11.2")
//    androidTestImplementation ("androidx.fragment:fragment-testing:1.3.6")

    debugImplementation("androidx.fragment:fragment-testing:1.1.0-beta01")
    debugImplementation("androidx.fragment:fragment-ktx:1.1.0-beta01")
    debugImplementation("androidx.test:core:1.2.0")
    debugImplementation("androidx.test:rules:1.2.0")
    debugImplementation("androidx.test:runner:1.2.0")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    //implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    //implementation(kotlin("script-runtime"))
    androidTestImplementation(libs.uiautomator)

}
