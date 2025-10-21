import io.github.diskria.projektor.common.licenses.LicenseType.MIT
import io.github.diskria.projektor.common.publishing.PublishingTargetType.MAVEN_CENTRAL
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
    version = "0.4.0"
    license = MIT
    publish = MAVEN_CENTRAL

    kotlinLibrary()
    AndroidLibraryConfigurator.applyRepositories(settings)
}
