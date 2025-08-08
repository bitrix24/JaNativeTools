plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.7.0"
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
        name = "JaNativeTools"
        id = "com.janative.tools"
        version = "1.0.5"
        vendor {
            name = "Bitrix"
            email = "susidskiy@bitrixsoft.com"
        }
        changeNotes = "Initial release"
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