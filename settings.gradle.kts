import io.github.diskria.projektor.common.licenses.MIT
import io.github.diskria.projektor.settings.configurators.AndroidLibraryConfigurator

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://diskria.github.io/projektor")
    }
}

plugins {
    id("io.github.diskria.projektor.settings") version "3.+"
}

projekt {
    description = "A Kotlin DSL for working with shell commands, Git, Android SDK tools, and ADB devices."
    version = "0.3.1"
    license = MIT
    tags = setOf("shell", "dsl")

    kotlinLibrary()
    AndroidLibraryConfigurator.applyRepositories(settings)
}
