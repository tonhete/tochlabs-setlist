import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Convencion para todo modulo KMP de la app: targets Android + iOS, SDK y tests comunes.
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

kotlin {
    iosArm64()
    iosSimulatorArm64()

    android {
        // core:model -> com.tochlabs.setlist.core.model
        namespace = "com.tochlabs.setlist." +
            project.path.removePrefix(":").replace(':', '.').replace('-', '_')
        compileSdk = catalog.findVersion("android-compileSdk").get().requiredVersion.toInt()
        minSdk = catalog.findVersion("android-minSdk").get().requiredVersion.toInt()
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        withHostTest { }
    }

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
