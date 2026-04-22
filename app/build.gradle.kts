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

// Google official SAMPLE IDs — used only for debug builds when no real ID is provided.
// NEVER used for release builds (see resolveAdMobId below).
private val TEST_ADMOB_APP_ID = "ca-app-pub-3940256099942544~3347511713"
private val TEST_ADMOB_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

fun resolveAdMobId(
    key: String,
    testFallback: String,
): String {
    val fromFile = secrets.getProperty(key)?.trim()?.takeIf { it.isNotEmpty() }
    val fromProject = (project.findProperty(key) as? String)?.trim()?.takeIf { it.isNotEmpty() }
    val fromEnv = System.getenv(key)?.trim()?.takeIf { it.isNotEmpty() }
    val provided = fromFile ?: fromProject ?: fromEnv
    if (provided != null) return provided

    // Release task present? → fail instead of baking in Google test IDs.
    val runningRelease =
        gradle.startParameter.taskNames.any {
            it.contains("Release", ignoreCase = true) ||
                it.contains("bundle", ignoreCase = true) ||
                it.contains("assembleRelease", ignoreCase = true)
        }
    if (runningRelease) {
        throw GradleException(
            "Missing $key for a release build. Provide it via:\n" +
                "  1) secrets.properties (gitignored, see secrets.properties.example)\n" +
                "  2) ./gradlew -P$key=...\n" +
                "  3) environment variable $key\n" +
                "Refusing to bake Google test ad IDs into a release build.",
        )
    }
    logger.warn(
        "[AdMob] $key not provided; falling back to Google SAMPLE test ID for a debug build.",
    )
    return testFallback
}

val admobAppId: String = resolveAdMobId("ADMOB_APP_ID", TEST_ADMOB_APP_ID)
val admobBannerAdUnitId: String = resolveAdMobId("ADMOB_BANNER_AD_UNIT_ID", TEST_ADMOB_BANNER_AD_UNIT_ID)

/** 스토어 스크린샷 등: ./gradlew assembleDebug -PADS_ENABLED=false */
val adsEnabled: Boolean =
    (project.findProperty("ADS_ENABLED")?.toString()?.lowercase() ?: "true") !in
        setOf("false", "0", "no", "off")

android {
    namespace = "net.gerosyab.magicball"
    compileSdk = 35

    defaultConfig {
        applicationId = "net.gerosyab.magicball"
        minSdk = 24
        targetSdk = 35
        versionCode = 4
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["admobAppId"] = admobAppId
        resValue("string", "admob_banner_ad_unit_id", admobBannerAdUnitId)
        buildConfigField("boolean", "ADS_ENABLED", adsEnabled.toString())
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
