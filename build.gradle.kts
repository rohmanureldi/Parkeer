plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.stability.analyzer) apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.firebase.perf) apply false
}

spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**")
        ktlint("1.5.0")
            .editorConfigOverride(
                mapOf(
                    "max_line_length" to "150",
                    "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
                    "ktlint_standard_no-wildcard-imports" to "disabled",
                ),
            )
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("**/build/**")
        ktlint("1.5.0")
    }
}
