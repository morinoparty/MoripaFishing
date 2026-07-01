import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import xyz.jpenilla.resourcefactory.paper.PaperPluginYaml

plugins {
    java
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
    alias(libs.plugins.resource.factory)
}

group = "party.morino"
version = project.version.toString()

dependencies {
    // Addon は公開アーティファクト (moripafishing-api) のみに依存する外部プラグイン想定。
    // :bukkit には依存しない (実際の外部リポジトリでは compileOnly("party.morino:moripafishing-api:<version>") に相当)。
    compileOnly(project(":api"))
    compileOnly(libs.paper.api)
    // RarityId (api) が KoinComponent を実装しているため、コンパイル時の型解決に必要。
    compileOnly(libs.koin.core)

    implementation(libs.bundles.kyori)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kaml)

    testImplementation(project(":api"))
    testImplementation(libs.paper.api)
    testImplementation(libs.koin.core)
    testImplementation(libs.mock.bukkit)
    testImplementation(platform("org.junit:junit-bom:6.0.3"))
    testImplementation(libs.bundles.junit.jupiter)
    testImplementation(libs.junit.platform.launcher)
    testImplementation(libs.allure.junit5)
}

kotlin {
    jvmToolchain(25)
}

tasks {
    compileKotlin {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_25)
        compilerOptions.javaParameters = true
    }
    build {
        dependsOn("shadowJar")
    }
    shadowJar {
        archiveClassifier.set("")
    }
    test {
        useJUnitPlatform()
        // Allure結果の出力先を指定
        systemProperty("allure.results.directory", "${project.layout.buildDirectory.get().asFile}/allure-results")
    }
}

sourceSets.main {
    resourceFactory {
        paperPluginYaml {
            name = "MoripaFishing-Addon-CatchAnnounce"
            version = project.version.toString()
            website = "https://fishing.plugin.morino.party"
            main = "party.morino.moripafishing.addons.catchannounce.CatchAnnouncePlugin"
            apiVersion = "1.20"
            dependencies {
                server("MoripaFishing", PaperPluginYaml.Load.BEFORE, required = false)
            }
        }
        bukkitPluginYaml {
            name = "MoripaFishing-Addon-CatchAnnounce"
            version = project.version.toString()
            website = "https://fishing.plugin.morino.party"
            main = "party.morino.moripafishing.addons.catchannounce.CatchAnnouncePlugin"
            apiVersion = "1.20"
            softDepend.set(listOf("MoripaFishing"))
        }
    }
}
