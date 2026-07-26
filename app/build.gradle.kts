import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

val keystoreProperties = Properties()
val keystorePropertiesFile = rootProject.file("keystore.properties")

if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(keystorePropertiesFile.inputStream())
}

// Load secrets from local.properties (gitignored — never committed)
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "com.balajitechlabs.quickdash"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.balajitechlabs.quickdash"
        minSdk = 24
        targetSdk = 36
        versionCode = 501
        versionName = "5.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Inject Telegram secrets from local.properties into BuildConfig
        buildConfigField(
            "String",
            "TG_BOT_TOKEN",
            "\"${localProperties.getProperty("TG_BOT_TOKEN", "")}\""
        )
        buildConfigField(
            "String",
            "TG_BROADCAST_BOT_TOKEN",
            "\"${localProperties.getProperty("TG_BROADCAST_BOT_TOKEN", "")}\""
        )
        buildConfigField(
            "String",
            "TG_CHAT_ID",
            "\"${localProperties.getProperty("TG_CHAT_ID", "")}\""
        )

        // Ensure full support for 64-bit & 32-bit architectures (no missing native library crashes)
        ndk {
            abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64"))
        }
    }

    val isBuildingBundle = gradle.startParameter.taskNames.any { it.contains("bundle", ignoreCase = true) }

    splits {
        abi {
            isEnable = !isBuildingBundle
            reset()
            include("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
            isUniversalApk = true
        }
    }

    signingConfigs {
//        getByName("debug") {
//            storeFile = rootProject.file("quickdash.jks")
//            storePassword = "quickdash"
//            keyAlias = "quickdash"
//            keyPassword = "quickdash"
//            enableV1Signing = true
//            enableV2Signing = true
//        }
        create("release") {
            if (keystoreProperties.isNotEmpty()) {
                storeFile = rootProject.file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
                enableV4Signing = true
            }
        }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true  // Required to generate BuildConfig fields
    }
}

ksp {
    arg("room.generateKotlin", "true")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended:1.6.0")
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.fragment:fragment-ktx:1.8.2")

    implementation("androidx.datastore:datastore-preferences:1.2.0")
    implementation("com.google.zxing:core:3.5.4")
    implementation("androidx.biometric:biometric:1.2.0-alpha05")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("nl.dionsegijn:konfetti-compose:2.0.4")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-config")
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Google Sign-In & Google Drive API for Cloud Sync
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.api-client:google-api-client-android:2.2.0")
    implementation("com.google.apis:google-api-services-drive:v3-rev20230822-2.0.0") {
        exclude(group = "org.apache.httpcomponents")
    }
    implementation("com.google.auth:google-auth-library-oauth2-http:1.19.0")

    // Custom UI & Theme Upgrades
    implementation("com.google.android.gms:play-services-code-scanner:16.1.0")
    implementation("androidx.graphics:graphics-shapes:1.0.1")
    implementation("androidx.palette:palette-ktx:1.0.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Jetpack Glance App Widget & Google Fonts
    implementation("androidx.glance:glance-appwidget:1.1.1")
    implementation("androidx.compose.ui:ui-text-google-fonts:1.6.0")

    // Google Play Core APIs: In-App Updates & In-App Reviews
    implementation("com.google.android.play:app-update-ktx:2.1.0")
    implementation("com.google.android.play:review-ktx:2.0.2")

    // MediaPipe LLM Inference (on-device AI with Gemma / Phi models)
    implementation("com.google.mediapipe:tasks-genai:0.10.22")

    
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
