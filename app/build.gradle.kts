import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.protobuf)
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
    ndkVersion = "28.0.13004108"

    defaultConfig {
        applicationId = "com.balajitechlabs.quickdash"
        minSdk = 24
        targetSdk = 36
        versionCode = 513
        versionName = "5.1.1"

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
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true  // Required to generate BuildConfig fields
    }
}


ksp {
    arg("room.generateKotlin", "true")
}

protobuf {
    protoc {
        artifact = libs.protobuf.protoc.get().toString()
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
            }
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended") // Version managed by Compose BOM
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("com.airbnb.android:lottie-compose:6.6.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.fragment:fragment-ktx:1.8.2")
    implementation("com.google.zxing:core:3.5.4")
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.core)
    implementation(libs.protobuf.javalite)

    // Hilt & Navigation
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    implementation("androidx.biometric:biometric:1.1.0") // Stable release — avoid alpha in production
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("nl.dionsegijn:konfetti-compose:2.0.4")
    implementation("androidx.security:security-crypto:1.1.0")
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-config")
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Room Database
    val roomVersion = "2.7.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Google Sign-In (kept for future use)
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // Custom UI & Theme Upgrades
    implementation("com.google.android.gms:play-services-code-scanner:16.1.0")
    implementation("androidx.graphics:graphics-shapes:1.0.1")
    implementation("androidx.palette:palette-ktx:1.0.0")
    implementation("androidx.appcompat:appcompat:1.7.0")

    // Jetpack Glance App Widget & Google Fonts
    implementation("androidx.glance:glance-appwidget:1.1.1")
    implementation("androidx.compose.ui:ui-text-google-fonts:1.6.0")

    // Google Play Core APIs: In-App Updates, In-App Reviews & Play Integrity
    implementation("com.google.android.play:app-update-ktx:2.1.0")
    implementation("com.google.android.play:review-ktx:2.0.2")
    implementation("com.google.android.play:integrity:1.4.0")

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

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.addAll(listOf("-Xmetadata-version=2.1.0", "-Xannotation-default-target=param-property"))
    }
}

