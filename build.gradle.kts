import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.21"
    id("org.jetbrains.intellij") version "1.17.4"
}

group = "com.terminalsnippets"
version = "1.0.4"

repositories {
    mavenCentral()
}

dependencies {
    // Gson for JSON import/export (bundled into the plugin)
    implementation("com.google.code.gson:gson:2.11.0")
}

intellij {
    // PhpStorm 2023.3 as build base; compatible with 2023.3 – 2025.x
    version.set("2023.3.4")
    type.set("PS")
    plugins.set(listOf("terminal"))
    downloadSources.set(false)
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("233")
        // No untilBuild set → no upper limit → compatible with all future versions
        untilBuild.set("")
    }

    publishPlugin {
        token.set(providers.gradleProperty("intellijPublishToken"))
    }
}
