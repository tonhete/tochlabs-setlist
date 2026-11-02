plugins {
    id("tochlabs.kmp.feature")
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            // FileProvider (share a file with other apps) and the document picker.
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.activity.compose)
        }
    }
}
