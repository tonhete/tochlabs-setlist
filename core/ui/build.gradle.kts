plugins {
    id("tochlabs.kmp.library")
    id("tochlabs.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
        }
    }
}
