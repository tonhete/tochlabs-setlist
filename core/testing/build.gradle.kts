// In-memory fakes of the core:domain repositories and test helpers. Used only from commonTest of other modules.
plugins {
    id("tochlabs.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))
            api(libs.kotlinx.coroutines.test)
            api(kotlin("test"))
        }
    }
}
