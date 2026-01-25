// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.13.2" apply false
    id("org.jetbrains.kotlin.android") version "2.3.0" apply false
    alias(libs.plugins.kotlin.compose) apply false

}
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.google.gms:google-services:4.4.0")
        classpath("com.android.tools.build:gradle:8.13")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.0")
        classpath("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.3.0")

    }
}
