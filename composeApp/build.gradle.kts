import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    kotlin("plugin.serialization") version "2.1.0"
    kotlin("plugin.power-assert") version "2.1.0"
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.koin.android)
        }

        commonMain.dependencies {
            implementation(libs.koin.core)
            implementation(libs.koin.compose)

            implementation(libs.cryptography.core)
            implementation(libs.cryptography.provider.jdk)

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)

            implementation(libs.kotlinx.collections.immutable)
            implementation(libs.kotlinx.serialization.json)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.junit)

            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)

            implementation(libs.koin.test)
            implementation(libs.koin.test.junit4)

            implementation(libs.mockk)
            implementation(libs.turbine)
            implementation(libs.fixture)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.linux_x64)
            implementation(compose.desktop.windows_x64)
            implementation(libs.kotlinx.coroutines.swing)
        }
    }
}

val appName = "AstroSign"

android {
    namespace = "org.astroboy.myaccounts"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.astroboy.myaccounts"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        setProperty("archivesBaseName", appName)
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            resValue("string", "app_name", "$appName Debug")
        }

        getByName("release") {
            isMinifyEnabled = false
            resValue("string", "app_name", appName)
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "org.astroboy.myaccounts.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = appName
            packageVersion = "1.0.0"
        }

        buildTypes.release.proguard {
            obfuscate = true
            configurationFiles.from(project.file("compose-desktop.pro"))
        }
    }
}
