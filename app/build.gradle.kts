import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.detekt)
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files("$rootDir/detekt.yml"))
    baseline = file("$rootDir/detekt-baseline.xml")
}

hilt {
    enableAggregatingTask = false
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
    compileSdk = 37

    defaultConfig {
        applicationId = "com.balajitechlabs.quickdash"
        minSdk = 26
        targetSdk = 37
        versionCode = 525
        versionName = "5.2.3"

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

    val storePass = System.getenv("KEYSTORE_PASSWORD") ?: localProperties.getProperty("KEYSTORE_PASSWORD")
    val alias = System.getenv("KEY_ALIAS") ?: localProperties.getProperty("KEY_ALIAS")
    val keyPass = System.getenv("KEY_PASSWORD") ?: localProperties.getProperty("KEY_PASSWORD")
    val storeFilePath = System.getenv("KEYSTORE_PATH") ?: localProperties.getProperty("KEYSTORE_PATH", "quickdash.jks")
    val hasKeystore = !storePass.isNullOrEmpty() && !alias.isNullOrEmpty() && !keyPass.isNullOrEmpty() && rootProject.file(storeFilePath).exists()

    signingConfigs {
        create("release") {
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
            keepDebugSymbols += listOf("**/libandroidx.graphics.path.so", "**/libdatastore_shared_counter.so")
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
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.lottie)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.zxing)
    implementation(libs.zxing.android.embedded)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.core)
    implementation(libs.protobuf.javalite)

    // Hilt & Navigation
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    // Security & Biometric
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.security.crypto)

    // Utilities & Parsing
    implementation(libs.gson)
    implementation(libs.jsoup)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.konfetti)
    implementation(libs.androidx.exifinterface)

    // Room Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // UI & Graphics
    implementation(libs.androidx.graphics.shapes)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Jetpack Glance App Widget & Google Fonts
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    implementation(libs.androidx.ui.text.google.fonts)

    // Coil Image & GIF Loading
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)

    // Shizuku Elevated Power-User Capabilities
    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)
    implementation(libs.hiddenapibypass)

    testImplementation(libs.junit)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.truth)
    androidTestImplementation(libs.truth)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xmetadata-version=2.1.0")
    }
}

