import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.cs407.cadence"
    compileSdk = 36

    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localProperties.load(FileInputStream(localPropertiesFile))
    }

    defaultConfig {
        applicationId = "com.cs407.cadence"
        minSdk = 34
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        manifestPlaceholders["spotifyClientId"] = localProperties.getProperty("SPOTIFY_CLIENT_ID") ?: ""
        manifestPlaceholders["redirectHostName"] = "callback"
        manifestPlaceholders["redirectSchemeName"] = "com.cs407.cadence.auth"
        manifestPlaceholders["appAuthRedirectScheme"] = "com.cs407.cadence"
        
        // Make Spotify client secret available at runtime
        buildConfigField("String", "SPOTIFY_CLIENT_SECRET", "\"${localProperties.getProperty("SPOTIFY_CLIENT_SECRET") ?: ""}\"")
        buildConfigField("String", "SPOTIFY_CLIENT_ID", "\"${localProperties.getProperty("SPOTIFY_CLIENT_ID") ?: ""}\"")
        buildConfigField("String", "ACOUSTID_API_KEY", "\"${localProperties.getProperty("ACOUSTID_API_KEY") ?: ""}\"")
        buildConfigField("String", "RAPIDAPI_KEY", "\"${localProperties.getProperty("RAPIDAPI_KEY") ?: ""}\"")
        // TheAudioDB uses API key "2" in the base URL (no BuildConfig needed)

        //Google Maps API key is added to manifest through a placeholder below.
        //Allows us to keep the key out of version control while
        //still injecting it safely into AndroidManifest.xml
        manifestPlaceholders["MAPS_API_KEY"] = localProperties.getProperty("MAPS_API_KEY") ?: ""
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation("com.adamratzman:spotify-remote-republish:1.1")
    implementation("com.google.code.gson:gson:2.6.1")
    
    // Spotify Web API dependencies
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

        // OAuth and browser authentication
        implementation("androidx.browser:browser:1.5.0")
        implementation("net.openid:appauth:0.11.1")
    
    // Image loading library
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx") //adding firestore dependency for storing workout sessions
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended-android:1.6.8")
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.ink.brush)
    implementation(libs.androidx.compose.foundation)
    //Required for GPS tracking functionality -- gives access to device's location provider
    implementation("com.google.android.gms:play-services-location:21.0.1")
    //Jetpack Compose wrapper for Google Maps SDK -- allows use of <GoogleMap> inside composables
    implementation("com.google.maps.android:maps-compose:4.1.1")
    //Core Google Maps library -- needed in order for Google Map tiles can render on MapScreen
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}