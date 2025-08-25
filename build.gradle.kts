import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML

plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.7.0"
    id("org.jetbrains.changelog") version "2.2.0"
    kotlin("jvm") version "2.1.20"
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

intellijPlatform {
    pluginConfiguration {
        id = providers.gradleProperty("pluginId")
        name = providers.gradleProperty("pluginName")
        version = providers.gradleProperty("pluginVersion")
        vendor {
            name = providers.gradleProperty("pluginVendorName")
            email = providers.gradleProperty("pluginVendorEmail")
        }

        val changelog = project.changelog
        changeNotes = providers.gradleProperty("pluginVersion").map { pluginVersion ->
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
        }
    }

    dependencies {
        intellijPlatform {
            bundledPlugins(
                listOf(
                    "JavaScript",
                    "com.intellij",
                    "com.jetbrains.php",
                    "com.intellij.css"
                )
            )
            phpstorm("2025.2")
//        local(providers.gradleProperty("phpStormPath")) // exception com.intellij.platform.core.nio.fs.MultiRoutingFileSystemProvide
        }
        implementation("org.json:json:20250107")
        implementation(kotlin("stdlib-jdk8"))
        testImplementation(kotlin("test"))
        testImplementation("io.mockk:mockk:1.14.2")
    }

    tasks {
        withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            compilerOptions {
                jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
            }
        }

        patchPluginXml {
            sinceBuild.set("241")
            untilBuild.set("252.*")
        }
        withType<JavaCompile> {
            sourceCompatibility = "21"
            targetCompatibility = "21"
        }
    }

    sourceSets {
        main {
            java.srcDirs("src/main/java", "src/main/kotlin")
        }
    }

    changelog {
        version.set(project.version.toString())
    }
}
