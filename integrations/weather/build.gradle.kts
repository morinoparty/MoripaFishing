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

    implementation(libs.koin.core)

    testImplementation(project(":integrations:weather-api"))
    testImplementation(libs.paper.api)
    testImplementation(libs.mock.bukkit)
    testImplementation(platform("org.junit:junit-bom:6.0.3"))
    testImplementation(libs.bundles.junit.jupiter)
    testImplementation(libs.junit.platform.launcher)
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
    }
}

sourceSets.main {
    resourceFactory {
        paperPluginYaml {
            name = "MoripaFishingWeather"
            version = project.version.toString()
            website = "https://fishing.plugin.morino.party"
            main = "party.morino.moripafishing.integrations.weather.MoripaFishingWeatherPlugin"
            apiVersion = "1.20"
        }
        bukkitPluginYaml {
            name = "MoripaFishingWeather"
            version = project.version.toString()
            website = "https://fishing.plugin.morino.party"
            main = "party.morino.moripafishing.integrations.weather.MoripaFishingWeatherPlugin"
            apiVersion = "1.20"
        }
    }
}
