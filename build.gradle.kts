// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
}
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.google.firebase.crashlytics) apply false
}
