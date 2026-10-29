plugins {
    `kotlin-dsl`
}

group = "com.tochlabs.setlist.buildlogic"

dependencies {
    // compileOnly: las versiones reales las carga el build raiz (plugins ... apply false).
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.kotlin.composeCompilerGradlePlugin)
}
