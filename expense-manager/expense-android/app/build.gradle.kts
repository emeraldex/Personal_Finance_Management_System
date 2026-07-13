plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.expense.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.expense.android"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }
    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    testOptions { unitTests { isIncludeAndroidResources = true } }
}

dependencies {
    // Reuse ALL business logic from the shared, pure-Java core.
    // expense-core is a Maven module; build & install it first with
    //   mvn -pl expense-core -am install
    // then consume the produced jar here. (In a real multi-repo setup the core
    // would be published to a Maven repository and referenced by coordinates.)
    implementation(files("../../expense-core/target/expense-core-1.0.0.jar"))
    // No JDBC driver on-device: persistence uses android.database.sqlite via the
    // Android*Repository adapters. (The core's JDBC classes remain in the jar but
    // are never loaded on Android.)

    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Local JVM tests for the SQLite adapters run real android.database.sqlite
    // via Robolectric — no emulator required.
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.12.2")
}
