import io.github.diskria.projektor.publishing.maven.MavenCentral

plugins {
    `maven-publish`
    signing
    alias(libs.plugins.projektor)
    alias(libs.plugins.build.config)
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.kotlin.utils)
    implementation(libs.kotlin.regex.dsl)

    implementation(gradleApi())

    compileOnly(libs.android.tools)
}

projekt {
    publishingTarget = MavenCentral

    kotlinLibrary()
}
