plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.multitoolx"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.multitoolx"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // 👇 Add this for custom APK name
    applicationVariants.configureEach {
        outputs.forEach { output ->
            if (output is com.android.build.gradle.internal.api.BaseVariantOutputImpl) {
                val appName = "MultiToolX"
                output.outputFileName = "$appName-${name}.apk"
            }
        }
    }
}

dependencies {
    //implementation(files("libs/app-debug.aar"))
    implementation ("org.nanohttpd:nanohttpd:2.3.1")
    //implementation(project(":unityLibrary"))
    //implementation(project(":launcher"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("com.google.android.material:material:1.9.0")

    implementation ("com.github.bumptech.glide:glide:4.14.2")

    implementation ("androidx.media3:media3-exoplayer:1.2.0")
    implementation ("androidx.media3:media3-ui:1.2.0")


    implementation ("androidx.media3:media3-exoplayer-dash:1.1.0")

    implementation ("com.github.QuadFlask:colorpicker:0.0.15")

    implementation ("com.github.CanHub:Android-Image-Cropper:4.3.2")


}