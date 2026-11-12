import xyz.jpenilla.resourcefactory.paper.PaperPluginYaml

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
    alias(libs.plugins.resource.factory)
}

dependencies {
    // Addon は公開アーティファクト (moripafishing-api) のみに依存する外部プラグイン想定。
    // :bukkit には依存しない (実際の外部リポジトリでは compileOnly("party.morino:moripafishing-api:<version>") に相当)。
    compileOnly(project(":api"))

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kaml)

    testImplementation(project(":api"))
}

sourceSets.main {
    resourceFactory {
        paperPluginYaml {
            name = "MoripaFishing-Addon-CatchAnnounce"
            version = project.version.toString()
            website = "https://fishing.plugin.morino.party"
            main = "party.morino.moripafishing.addons.catchannounce.CatchAnnouncePlugin"
            apiVersion = "26.2"
            dependencies {
                server("MoripaFishing", PaperPluginYaml.Load.BEFORE, required = true)
            }
        }
    }
}
