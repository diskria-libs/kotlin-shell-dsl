plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.dokka)
    `maven-publish`
    signing
}

group = "io.github.diskria"
version = "0.1.0"

kotlin {
    jvmToolchain(libs.versions.java.get().toInt())
}

java {
    withSourcesJar()
}

tasks.register<Jar>("javadocJar") {
    dependsOn(tasks.dokkaJavadoc)
    archiveClassifier.set("javadoc")
    from(tasks.dokkaJavadoc)
}

dependencies {
    implementation(libs.kotlin.utils)
    implementation(libs.kotlin.regex.dsl)

    implementation(gradleApi())

    compileOnly(libs.android.tools)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks.named("javadocJar"))
            artifactId = "kotlin-shell-dsl"

            pom {
                name.set("kotlin-shell-dsl")
                description.set("Kotlin shell DSL")
                url.set("https://github.com/diskria-libs/kotlin-shell-dsl")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("diskria")
                        name.set("diskria")
                        email.set("diskreee@gmail.com")
                    }
                }
                scm {
                    url.set("https://github.com/diskria-libs/kotlin-shell-dsl")
                    connection.set("scm:git:https://github.com/diskria-libs/kotlin-shell-dsl.git")
                    developerConnection.set("scm:git:git@github.com:diskria-libs/kotlin-shell-dsl.git")
                }
            }
        }
    }

    repositories {
        maven {
            name = "stagingLocal"
            url = layout.buildDirectory.dir("staging-repo").get().asFile.toURI()
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["mavenJava"])
}

tasks.register<Zip>("bundleForCentral") {
    group = "publishing"
    dependsOn("clean", "dokkaJavadoc", "publish")
    from(layout.buildDirectory.dir("staging-repo"))
    destinationDirectory.set(layout.buildDirectory.dir("bundle"))
    archiveFileName.set("bundle.zip")
}
