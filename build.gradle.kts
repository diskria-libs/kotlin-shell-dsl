import io.github.diskria.projektor.extensions.configureLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `maven-publish`
    signing
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.projektor)
}

dependencies {
    implementation(libs.kotlin.utils)
    implementation(libs.kotlin.regex.dsl)

    implementation(gradleApi())

    compileOnly(libs.android.tools)
}

configureLibrary(jvmTarget = JvmTarget.JVM_11)
