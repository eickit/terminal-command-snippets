import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.21"
    id("org.jetbrains.intellij") version "1.17.4"
}

group = "com.terminalsnippets"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // Gson für JSON-Import/Export (wird ins Plugin gebündelt)
    implementation("com.google.code.gson:gson:2.11.0")
}

intellij {
    // PhpStorm 2023.3 als Build-Basis; kompatibel mit 2023.3 – 2025.x
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
        // Kein untilBuild gesetzt → keine Obergrenze → kompatibel mit allen zukünftigen Versionen
        untilBuild.set("")
    }
}
