import xyz.jpenilla.resourcefactory.paper.PaperPluginYaml

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
    alias(libs.plugins.resource.factory)
}

dependencies {
    // コアの公開 API (SPI を含む)。実行時は導入済みのコア本体のクラスを使うため、同梱しない。
    compileOnly(project(":api"))

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kaml)

    testImplementation(project(":api"))
}

// paper-plugin.yml のプラグイン名。配布する JAR のファイル名にも使う
val pluginName = "MoripaFishing-Integration-WorldLifecycle"

tasks.shadowJar {
    archiveFileName = "${pluginName}_${project.version}.jar"
}

sourceSets.main {
    resourceFactory {
        paperPluginYaml {
            name = pluginName
            version = project.version.toString()
            website = "https://fishing.plugin.morino.party"
            main = "party.morino.moripafishing.integrations.worldlifecycle.MoripaFishingWorldLifecyclePlugin"
            apiVersion = "26.3"
            dependencies {
                // コアの後にロードし、コアのクラス (SPI) を join-classpath で参照する
                server("MoripaFishing", PaperPluginYaml.Load.BEFORE, required = true)
            }
        }
    }
}
