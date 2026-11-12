import xyz.jpenilla.resourcefactory.paper.PaperPluginYaml

plugins {
    java
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.shadow)
    alias(libs.plugins.resource.factory)
}

group = "party.morino"
version = project.version.toString()

dependencies {
    // コアの公開 API (SPI を含む)。実行時は導入済みのコア本体のクラスを使うため、同梱しない。
    compileOnly(project(":api"))
    compileOnly(libs.paper.api)

    testImplementation(project(":api"))
    testImplementation(libs.paper.api)
    testImplementation(libs.mock.bukkit)
    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation(libs.bundles.junit.jupiter)
    testImplementation(libs.junit.platform.launcher)
    testImplementation(libs.allure.junit5)
}

tasks {
    build {
        dependsOn("shadowJar")
    }
    jar {
        // 依存を同梱しない jar。配布物 (shadowJar) と同名にならないよう分類子を付ける
        archiveClassifier.set("plain")
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
            dependencies {
                // コアの後にロードし、コアのクラス (SPI) を join-classpath で参照する
                server("MoripaFishing", PaperPluginYaml.Load.BEFORE, required = true)
            }
        }
        bukkitPluginYaml {
            name = "MoripaFishing-Integration-Weather"
            version = project.version.toString()
            website = "https://fishing.plugin.morino.party"
            main = "party.morino.moripafishing.integrations.weather.MoripaFishingWeatherPlugin"
            apiVersion = "1.20"
            depend.set(listOf("MoripaFishing"))
        }
    }
}
