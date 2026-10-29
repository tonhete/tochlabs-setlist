// Convencion para modulos feature:*  (UI Compose + acceso a core:model, core:domain y core:ui).
plugins {
    id("tochlabs.kmp.library")
    id("tochlabs.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:model"))
            implementation(project(":core:domain"))
            implementation(project(":core:ui"))
        }
    }
}
