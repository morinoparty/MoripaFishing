import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    java
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

group = "party.morino"
version = project.version.toString()

dependencies {
    // `GeneratorData` の `@Serializable` のみのために含める。`:api` への依存は持たない。
    implementation(libs.kotlinx.serialization.json)
}

tasks {
    compileKotlin {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
        compilerOptions.javaParameters = true
    }
}

repositories {
    mavenCentral()
}
