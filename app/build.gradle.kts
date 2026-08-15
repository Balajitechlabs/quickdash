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

// Secrets from local.properties (gitignored — never committed)
// For CI, set KEYSTORE_PASSWORD / KEY_ALIAS / KEY_PASSWORD / KEYSTORE_PATH as GitHub Secrets
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "com.balajitechlabs.quickdash"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.balajitechlabs.quickdash"
        minSdk = 24
        targetSdk = 36
        versionCode = 521
        versionName = "5.2.1"

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

        // Only include real-device architectures (arm64-v8a + armeabi-v7a)
        // x86 / x86_64 dropped — these are emulator-only and bloat the bundle
        ndk {
            abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a"))
        }
    }

    val isBuildingBundle = gradle.startParameter.taskNames.any { it.contains("bundle", ignoreCase = true) }

    splits {
        abi {
            isEnable = !isBuildingBundle
            reset()
            include("arm64-v8a", "armeabi-v7a")
            isUniversalApk = true
        }
    }

    signingConfigs {
        create("release") {
            val storePass = System.getenv("KEYSTORE_PASSWORD") ?: localProperties.getProperty("KEYSTORE_PASSWORD")
            val alias = System.getenv("KEY_ALIAS") ?: localProperties.getProperty("KEY_ALIAS")
            val keyPass = System.getenv("KEY_PASSWORD") ?: localProperties.getProperty("KEY_PASSWORD")
            val storeFilePath = System.getenv("KEYSTORE_PATH") ?: localProperties.getProperty("KEYSTORE_PATH", "quickdash.jks")

            val hasKeystore = !storePass.isNullOrEmpty() && !alias.isNullOrEmpty() && !keyPass.isNullOrEmpty() && rootProject.file(storeFilePath).exists()
            if (hasKeystore) {
                storeFile = rootProject.file(storeFilePath)
                storePassword = storePass
                keyAlias = alias
                keyPassword = keyPass
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
                enableV4Signing = true
            }
        }
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
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
            val storePass = System.getenv("KEYSTORE_PASSWORD") ?: localProperties.getProperty("KEYSTORE_PASSWORD")
            val alias = System.getenv("KEY_ALIAS") ?: localProperties.getProperty("KEY_ALIAS")
            val keyPass = System.getenv("KEY_PASSWORD") ?: localProperties.getProperty("KEY_PASSWORD")
            val storeFilePath = System.getenv("KEYSTORE_PATH") ?: localProperties.getProperty("KEYSTORE_PATH", "quickdash.jks")
            val hasKeystore = !storePass.isNullOrEmpty() && !alias.isNullOrEmpty() && !keyPass.isNullOrEmpty() && rootProject.file(storeFilePath).exists()

            signingConfig = if (hasKeystore) signingConfigs.getByName("release") else signingConfigs.getByName("debug")
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

    lint {
        abortOnError = false
        checkReleaseBuilds = false
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
    implementation("androidx.core:core-splashscreen:1.2.0")
    implementation(libs.lottie)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.fragment:fragment-ktx:1.8.2")
    implementation(libs.zxing)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.core)
    implementation(libs.protobuf.javalite)

    // Hilt & Navigation
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.biometric)
    implementation(libs.gson)
    implementation(libs.workmanager)
    implementation(libs.konfetti)
    implementation("androidx.security:security-crypto:1.1.0")
    implementation(platform(libs.firebase.bom))
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-config")
    implementation(libs.coil)

    // Room Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Google Sign-In (kept for future use)
    implementation("com.google.android.gms:play-services-auth:21.6.0")

    // Custom UI & Theme Upgrades
    implementation("com.google.android.gms:play-services-code-scanner:16.1.0")
    implementation("androidx.graphics:graphics-shapes:1.1.0")
    implementation("androidx.palette:palette-ktx:1.0.0")
    implementation("androidx.appcompat:appcompat:1.7.0")

    // Jetpack Glance App Widget & Google Fonts
    implementation("androidx.glance:glance-appwidget:1.1.1")
    implementation("androidx.glance:glance-material3:1.1.1")
    implementation("androidx.compose.ui:ui-text-google-fonts:1.6.0")

    // Google Play Core APIs: In-App Updates, In-App Reviews & Play Integrity
    implementation("com.google.android.play:app-update-ktx:2.1.0")
    implementation("com.google.android.play:review-ktx:2.0.2")
    implementation("com.google.android.play:integrity:1.4.0")

    // MediaPipe LLM Inference (on-device AI with Gemma / Phi models)
    implementation("com.google.mediapipe:tasks-genai:0.10.35")

    
    testImplementation(libs.junit)
    testImplementation("app.cash.turbine:turbine:1.2.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("com.google.truth:truth:1.4.4")
    androidTestImplementation("com.google.truth:truth:1.4.4")
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.addAll(listOf("-Xmetadata-version=2.1.0"))
    }
}

