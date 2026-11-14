import xyz.jpenilla.resourcefactory.bukkit.Permission
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

// paper-plugin.yml のプラグイン名。配布する JAR のファイル名にも使う
val pluginName = "MoripaFishing-Addon-Chum"

tasks.shadowJar {
    archiveFileName = "${pluginName}_${project.version}.jar"
}

sourceSets.main {
    resourceFactory {
        paperPluginYaml {
            name = pluginName
            version = project.version.toString()
            website = "https://fishing.plugin.morino.party"
            main = "party.morino.moripafishing.addons.chum.ChumPlugin"
            apiVersion = "26.3"
            dependencies {
                server("MoripaFishing", PaperPluginYaml.Load.BEFORE, required = true)
            }
            permissions {
                register("moripafishing.addon.chum.use") {
                    description = "Use chum items"
                    default = Permission.Default.TRUE
                }
                register("moripafishing.addon.chum.give") {
                    description = "Give chum items with /chum give"
                    default = Permission.Default.OP
                }
                register("moripafishing.addon.chum.odds") {
                    description = "Show your own catch probabilities with /chum odds"
                    default = Permission.Default.OP
                }
                register("moripafishing.addon.chum.odds.others") {
                    description = "Show another player's catch probabilities with /chum odds <player>"
                    default = Permission.Default.OP
                }
                register("moripafishing.addon.chum.reload") {
                    description = "Reload the chum config with /chum reload"
                    default = Permission.Default.OP
                }
            }
        }
    }
}
