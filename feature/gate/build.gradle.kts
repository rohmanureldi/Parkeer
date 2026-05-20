plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.eldirohmanur.parkeer.feature.gate"
    compileSdk = 36
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlin {
        compilerOptions {
            languageVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0
            jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
        }
    }
    buildFeatures { compose = true }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:nfc"))
    implementation(project(":core:ui"))
    implementation(project(":core:firebase"))
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.navigation)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.telkomsel.dexterity)
    implementation(libs.dotlottie.android)
    ksp(libs.hilt.compiler)
    lintChecks(libs.compose.lint.checks)
}
