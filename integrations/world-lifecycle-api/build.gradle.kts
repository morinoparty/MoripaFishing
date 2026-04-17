import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    java
    alias(libs.plugins.kotlin.jvm)
}

group = "party.morino"
version = project.version.toString()

dependencies {
    // `FishingWorldId`, `GeneratorData`, `GeneratorId` の型参照用。
    // 実行時は core または integration plugin が shaded している :api 由来クラスが使われる。
    compileOnly(project(":api"))
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
