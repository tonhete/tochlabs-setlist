// Modulo raiz de la app (equivale al antiguo composeApp): UI raiz, navegacion y DI.
// Es tambien el framework "Shared" que consume iosApp.
plugins {
    id("tochlabs.kmp.library")
    id("tochlabs.compose")
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    android {
        androidResources {
            enable = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
        }
        commonMain.dependencies {
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation(project(":core:model"))
            implementation(project(":core:domain"))
            implementation(project(":core:data"))
            implementation(libs.koin.core)
            implementation(project(":core:ui"))

            implementation(project(":feature:setlists"))
            implementation(project(":feature:songs"))
            implementation(project(":feature:bands"))
            implementation(project(":feature:calendar"))
            implementation(project(":feature:stage"))
            implementation(project(":feature:share"))
        }
    }
}

dependencies {
    add("androidRuntimeClasspath", libs.compose.uiTooling)
}
