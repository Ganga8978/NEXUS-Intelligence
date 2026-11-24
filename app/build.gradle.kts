plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
    namespace = "com.nexus.classic"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.nexus.classic"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }
}
