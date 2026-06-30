import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    java
    alias(libs.plugins.kotlin.jvm)
}

group = "party.morino"
version = project.version.toString()

dependencies {
    // SPI のみを提供するモジュール。`:api` にも Bukkit にも依存しない。
}

tasks {
    compileKotlin {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_25)
        compilerOptions.javaParameters = true
    }
}

repositories {
    mavenCentral()
}
