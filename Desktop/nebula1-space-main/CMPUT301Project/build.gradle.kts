// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
}

buildscript {
    repositories {
        google()
        mavenCentral() // Add mavenCentral if it’s missing
    }
    dependencies {
        val nav_version = "2.7.0"
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:$nav_version")
        classpath("com.android.tools.build:gradle:8.0.0") // Use the appropriate version
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.0") // Use the appropriate version
    }
}
