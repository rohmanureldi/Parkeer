plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.stability.analyzer)
    id("com.google.gms.google-services")
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.perf)
    jacoco
}

jacoco {
    toolVersion = "0.8.12"
}

val exclusions =
    listOf(
        "**/R.class",
        "**/R\$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "**/di/**",
        "**/*_Hilt*.*",
        "**/*_Factory*.*",
        "**/*_MembersInjector*.*",
        "**/Hilt_*.*",
        "**/*Screen*.*",
        "**/*Composable*.*",
        "**/*Preview*.*",
        "**/*Activity*.*",
        "**/*Application*.*",
        "**/*Navigation*.*",
        "**/components/**",
        "**/navigation/**",
        "**/screen/**",
        "**/security/**",
        "**/NtagCardReader*.*",
        "**/Firebase*.*",
        "**/AnalyticsInitializer*.*",
        "**/LocalAnalytics*.*",
        "**/NativeCipher*.*",
        "**/KeyStoreManager*.*",
    )

android {
    namespace = "com.eldirohmanur.parkeer"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.eldirohmanur.parkeer"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        debug {
            enableAndroidTestCoverage = true
            enableUnitTestCoverage = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            languageVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0
            jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
        }
    }

    lint {
        baseline = file("lint-baseline.xml")
        abortOnError = false
    }

    @Suppress("UnstableApiUsage")
    testOptions { unitTests.all { it.useJUnitPlatform() } }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

tasks.withType<Test> {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

// Collect source dirs from all modules
val moduleSourceDirs =
    listOf(
        "core/model",
        "core/nfc",
        "core/crypto",
        "core/cardprotocol",
        "core/firebase",
        "feature/station",
        "feature/gate",
        "feature/terminal",
        "feature/scout",
    ).map { rootProject.file("$it/src/main/java") }

// Collect class dirs from all modules
fun classTree(buildDir: File) =
    listOf(
        fileTree("$buildDir/intermediates/javac/debug") { exclude(exclusions) },
        fileTree("$buildDir/tmp/kotlin-classes/debug") { exclude(exclusions) },
    )

val moduleBuildDirs =
    listOf(
        "core/model",
        "core/nfc",
        "core/crypto",
        "core/cardprotocol",
        "core/firebase",
        "feature/station",
        "feature/gate",
        "feature/terminal",
        "feature/scout",
    ).map { rootProject.file("$it/build") } + listOf(layout.buildDirectory.get().asFile)

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn(
        "testDebugUnitTest",
        ":feature:station:testDebugUnitTest",
        ":feature:gate:testDebugUnitTest",
        ":feature:terminal:testDebugUnitTest",
        ":feature:scout:testDebugUnitTest",
        ":core:cardprotocol:testDebugUnitTest",
        ":core:crypto:testDebugUnitTest",
    )
    group = "Reporting"
    description = "Generate combined Jacoco coverage report for all modules"

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    sourceDirectories.setFrom(files(moduleSourceDirs + layout.projectDirectory.dir("src/main/java")))
    classDirectories.setFrom(files(moduleBuildDirs.flatMap { classTree(it) }))
    executionData.setFrom(
        files(
            moduleBuildDirs.map {
                fileTree(it) {
                    include(
                        "**/*.exec",
                        "**/*.ec",
                    )
                }
            },
        ),
    )
}

tasks.register<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn("jacocoTestReport")
    group = "Verification"
    description = "Verify code coverage against defined thresholds"

    violationRules {
        rule {
            element = "CLASS"
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = BigDecimal("0.60")
            }
            excludes =
                listOf(
                    "*.di.*",
                    "*.MainActivity",
                    "*.ParkeerApp",
                )
        }
    }

    classDirectories.setFrom(files(moduleBuildDirs.flatMap { classTree(it) }))
    executionData.setFrom(
        files(
            moduleBuildDirs.map {
                fileTree(it) {
                    include(
                        "**/*.exec",
                        "**/*.ec",
                    )
                }
            },
        ),
    )
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:nfc"))
    implementation(project(":core:crypto"))
    implementation(project(":core:cardprotocol"))
    implementation(project(":core:firebase"))
    implementation(project(":core:ui"))
    implementation(project(":feature:station"))
    implementation(project(":feature:gate"))
    implementation(project(":feature:terminal"))
    implementation(project(":feature:scout"))

    implementation(libs.core.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.navigation)
    implementation(libs.integrity)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.telkomsel.dexterity)
    implementation(libs.play.integrity)
    ksp(libs.hilt.compiler)
    lintChecks(libs.compose.lint.checks)

    debugImplementation(libs.compose.ui.tooling)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.coroutines.test)
}
