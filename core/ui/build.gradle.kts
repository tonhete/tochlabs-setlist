plugins {
    id("tochlabs.kmp.library")
    id("tochlabs.compose")
}

kotlin {
    android {
        androidResources {
            enable = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
            api(libs.compose.components.resources)
            implementation(libs.reorderable)
        }
    }
}

// Shared strings and icons, used by the feature modules.
compose.resources {
    publicResClass = true
    packageOfResClass = "com.tochlabs.setlist.core.ui.resources"
}
