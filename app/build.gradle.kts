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
        targetSdk = 35
        versionCode = 104
        versionName = "4.3.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Inject Telegram secrets from local.properties into BuildConfig
        // These are NOT embedded as plain strings — they come from the gitignored local.properties
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
        buildConfigField(
            "String",
            "ONESIGNAL_APP_ID",
            "\"${localProperties.getProperty("ONESIGNAL_APP_ID", "12d6faa7-b7b1-497d-b5d7-af1204458711")}\""
        )
    }

    signingConfigs {
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
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
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
    implementation("com.onesignal:OneSignal:[5.0.0, 5.99.99]")
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
    implementation("androidx.glance:glance-appwidget:1.1.0")
    implementation("androidx.compose.ui:ui-text-google-fonts:1.6.0")

    
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
