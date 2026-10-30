// Convencion para modulos feature:*  (UI Compose + acceso a core:model, core:domain y core:ui).
plugins {
    id("tochlabs.kmp.library")
    id("tochlabs.compose")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

kotlin {
    android {
        androidResources {
            enable = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:model"))
            implementation(project(":core:domain"))
            implementation(project(":core:ui"))
            implementation(catalog.findLibrary("compose-components-resources").get())
            implementation(catalog.findLibrary("androidx-lifecycle-viewmodel").get())
            implementation(catalog.findLibrary("androidx-lifecycle-viewmodelCompose").get())
            implementation(catalog.findLibrary("androidx-lifecycle-runtimeCompose").get())
            implementation(catalog.findLibrary("koin-core").get())
            implementation(catalog.findLibrary("koin-compose-viewmodel").get())
        }
        commonTest.dependencies {
            implementation(project(":core:testing"))
        }
    }
}

// Feature strings: feature:bands -> com.tochlabs.setlist.feature.bands.resources.Res
compose.resources {
    packageOfResClass = "com.tochlabs.setlist." +
        project.path.removePrefix(":").replace(':', '.').replace('-', '_') + ".resources"
}
