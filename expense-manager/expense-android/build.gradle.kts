// Root Gradle build. The Android app reuses the pure-Java `expense-core`
// (domain, services, analytics, reporting) and supplies Android-SQLite-backed
// implementations of the core repository ports.
plugins {
    id("com.android.application") version "8.5.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
}
