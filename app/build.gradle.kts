import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.sonexa.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.sonexa.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 24
        versionName = "2.4.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val localProps = Properties()
        val localFile = rootProject.file("local.properties")
        if (localFile.exists()) {
            localFile.inputStream().use { localProps.load(it) }
        }
        val googleWebClientId = localProps.getProperty("google.web.client.id", "")
        val appleServiceId = localProps.getProperty("apple.service.id", "")
        val appleRedirectUri = localProps.getProperty(
            "apple.redirect.uri",
            "com.sonexa.app://auth/apple"
        )
        val youtubeApiKey = localProps.getProperty("youtube.api.key", "")
        val jamendoClientId = localProps.getProperty("jamendo.client.id", "")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"$googleWebClientId\"")
        buildConfigField("String", "APPLE_SERVICE_ID", "\"$appleServiceId\"")
        buildConfigField("String", "APPLE_REDIRECT_URI", "\"$appleRedirectUri\"")
        buildConfigField("String", "YOUTUBE_API_KEY", "\"$youtubeApiKey\"")
        buildConfigField("String", "JAMENDO_CLIENT_ID", "\"$jamendoClientId\"")
    }

    buildTypes {
        debug {
            val localProps = Properties()
            val localFile = rootProject.file("local.properties")
            if (localFile.exists()) {
                localFile.inputStream().use { localProps.load(it) }
            }
            val apiBaseUrl = localProps.getProperty(
                "api.base.url",
                "http://10.0.2.2:8080/api/v1/"
            )
            buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
            manifestPlaceholders["networkSecurityConfig"] = "@xml/network_security_config_debug"
        }
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val localProps = Properties()
            val localFile = rootProject.file("local.properties")
            if (localFile.exists()) {
                localFile.inputStream().use { localProps.load(it) }
            }
            val candidate = (localProps.getProperty("api.base.url.release")
                ?: localProps.getProperty("api.base.url")
                ?: "https://api.zynera.app/api/v1/").trim()
            val releaseUrl = if (candidate.startsWith("https://") || candidate.startsWith("http://")) {
                candidate
            } else {
                "https://api.zynera.app/api/v1/"
            }
            val normalized = if (releaseUrl.endsWith("/")) releaseUrl else "$releaseUrl/"
            buildConfigField("String", "API_BASE_URL", "\"$normalized\"")
            manifestPlaceholders["networkSecurityConfig"] = "@xml/network_security_config"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.coil.compose)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.google.id)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.security.crypto)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
