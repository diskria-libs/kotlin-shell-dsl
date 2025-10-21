plugins {
    alias(libs.plugins.projektor)
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.kotlin.utils)
    implementation(libs.kotlin.regex.dsl)

    implementation(gradleApi())

    compileOnly(libs.android.tools)
}

projekt {
    kotlinLibrary()
}
