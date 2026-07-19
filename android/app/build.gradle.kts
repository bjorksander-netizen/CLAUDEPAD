import java.net.URL

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.bjorn.claudepad"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.bjorn.claudepad"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
        versionName = "2.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}

/**
 * Unduh JetBrains Mono ke res/font sebelum resource dikompilasi.
 * Kalau unduhan gagal (offline), build tetap berhasil dan aplikasi
 * otomatis memakai monospace bawaan sistem (lihat Fonts.kt).
 */
val fetchFont by tasks.registering {
    val outFile = file("src/main/res/font/jetbrains_mono.ttf")
    outputs.file(outFile)
    onlyIf { !outFile.exists() }
    doLast {
        val url = "https://raw.githubusercontent.com/JetBrains/JetBrainsMono/" +
                  "v2.304/fonts/ttf/JetBrainsMono-Regular.ttf"
        try {
            outFile.parentFile.mkdirs()
            URL(url).openStream().use { input ->
                outFile.outputStream().use { output -> input.copyTo(output) }
            }
            logger.lifecycle("fetchFont: JetBrains Mono berhasil diunduh")
        } catch (e: Exception) {
            logger.warn("fetchFont: gagal mengunduh font (${e.message}); " +
                        "aplikasi akan memakai monospace bawaan sistem")
        }
    }
}

tasks.configureEach {
    if (name.startsWith("generate") && name.endsWith("Resources")) {
        dependsOn(fetchFont)
    }
    if (name.startsWith("merge") && name.endsWith("Resources")) {
        dependsOn(fetchFont)
    }
}
