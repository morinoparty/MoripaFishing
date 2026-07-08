plugins {
    java
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.shadow)
    alias(libs.plugins.resource.factory)
}

group = "party.morino"
version = project.version.toString()

dependencies {
    // SPI モジュール: `WeatherControlProvider` を提供する。
    // core 側は compileOnly のみなので、本プラグインが唯一 shaded する責任を持つ。
    implementation(project(":integrations:weather-api"))
    compileOnly(libs.paper.api)

    testImplementation(project(":integrations:weather-api"))
    testImplementation(libs.paper.api)
    testImplementation(libs.mock.bukkit)
    testImplementation(platform("org.junit:junit-bom:6.0.3"))
    testImplementation(libs.bundles.junit.jupiter)
    testImplementation(libs.junit.platform.launcher)
    testImplementation(libs.allure.junit5)
}

tasks {
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
            name = "MoripaFishing-Integration-Weather"
            version = project.version.toString()
            website = "https://fishing.plugin.morino.party"
            main = "party.morino.moripafishing.integrations.weather.MoripaFishingWeatherPlugin"
            apiVersion = "1.20"
        }
        bukkitPluginYaml {
            name = "MoripaFishing-Integration-Weather"
            version = project.version.toString()
            website = "https://fishing.plugin.morino.party"
            main = "party.morino.moripafishing.integrations.weather.MoripaFishingWeatherPlugin"
            apiVersion = "1.20"
        }
    }
}
