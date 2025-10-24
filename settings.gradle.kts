import io.github.diskria.projektor.common.licenses.LicenseType.MIT
import io.github.diskria.projektor.common.publishing.PublishingTargetType.MAVEN_CENTRAL
import io.github.diskria.projektor.settings.configurators.AndroidLibraryConfigurator

pluginManagement {
    repositories {
        maven("https://diskria.github.io/projektor")
        gradlePluginPortal()
    }
}

plugins {
    id("io.github.diskria.projektor.settings") version "3.+"
}

projekt {
    version = "0.4.2"
    license = MIT
    publish = setOf(MAVEN_CENTRAL)

    kotlinLibrary()
    AndroidLibraryConfigurator.applyRepositories(settings)
}
