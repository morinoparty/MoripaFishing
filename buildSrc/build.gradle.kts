import org.gradle.kotlin.dsl.implementation

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0")
    }
}

repositories {
    mavenCentral()

    maven("https://repo.papermc.io/repository/maven-public/")

}

dependencies {
    implementation(gradleApi())
    implementation(localGroovy())
    implementation(libs.kotlinx.serialization.json)
    implementation("com.google.code.gson:gson:2.11.0")
    compileOnly(libs.paper.api)

        implementation(kotlin("reflect"))
}

