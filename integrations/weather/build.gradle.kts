import xyz.jpenilla.resourcefactory.paper.PaperPluginYaml

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.shadow)
    alias(libs.plugins.resource.factory)
}

dependencies {
    // コアの公開 API (SPI を含む)。実行時は導入済みのコア本体のクラスを使うため、同梱しない。
    compileOnly(project(":api"))

    testImplementation(project(":api"))
}

sourceSets.main {
    resourceFactory {
        paperPluginYaml {
            name = "MoripaFishing-Integration-Weather"
            version = project.version.toString()
            website = "https://fishing.plugin.morino.party"
            main = "party.morino.moripafishing.integrations.weather.MoripaFishingWeatherPlugin"
            apiVersion = "26.2"
            dependencies {
                // コアの後にロードし、コアのクラス (SPI) を join-classpath で参照する
                server("MoripaFishing", PaperPluginYaml.Load.BEFORE, required = true)
            }
        }
    }
}
