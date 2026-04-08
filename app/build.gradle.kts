import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val secretsFile = rootProject.file("secrets.properties")
val secrets = Properties().apply {
    if (secretsFile.exists()) {
        secretsFile.inputStream().use { load(it) }
    }
}

val admobAppId: String =
    secrets.getProperty("ADMOB_APP_ID")
        ?: (project.findProperty("ADMOB_APP_ID") as? String)
        ?: "ca-app-pub-3940256099942544~3347511713"

val admobBannerAdUnitId: String =
    secrets.getProperty("ADMOB_BANNER_AD_UNIT_ID")
        ?: (project.findProperty("ADMOB_BANNER_AD_UNIT_ID") as? String)
        ?: "ca-app-pub-3940256099942544/6300978111"

android {
    namespace = "net.gerosyab.magicball"
    compileSdk = 35

    defaultConfig {
        applicationId = "net.gerosyab.magicball"
        minSdk = 24
        targetSdk = 35
        versionCode = 3
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["admobAppId"] = admobAppId
        resValue("string", "admob_banner_ad_unit_id", admobBannerAdUnitId)
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.fragment:fragment-ktx:1.8.5")
    implementation("com.google.android.gms:play-services-ads:23.6.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
