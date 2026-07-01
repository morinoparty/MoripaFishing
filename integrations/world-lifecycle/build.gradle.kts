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
    // SPI モジュール: `WorldLifecycleProvider` / `GeneratorData` を提供する。
    // core 側は compileOnly のみなので、本プラグインが唯一 shaded する責任を持つ。
    implementation(project(":integrations:world-lifecycle-api"))
    // 参加時テレポート機能でコアの公開 API (`WorldManager` / イベント) を参照する。
    // 実行時は導入済みのコア本体を利用するため compileOnly に留める。
    compileOnly(project(":api"))
    compileOnly(libs.paper.api)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kaml)
    implementation(libs.koin.core)

    testImplementation(project(":integrations:world-lifecycle-api"))
    testImplementation(project(":api"))
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
            name = "MoripaFishing-Integration-WorldLifecycle"
            version = project.version.toString()
            website = "https://fishing.plugin.morino.party"
            main = "party.morino.moripafishing.integrations.worldlifecycle.MoripaFishingWorldLifecyclePlugin"
            apiVersion = "1.20"
            dependencies {
                // 参加時テレポートでコアの公開 API / イベントクラスへ実行時アクセスするため、
                // join-classpath (既定 true) 目的で依存を宣言する。
                // コア側は本 Integration を Load.BEFORE で参照するので、ここで順序を課すと循環する。
                // そのため Load.OMIT で順序制約を付けず、classpath 共有のみを目的とする。
                server("MoripaFishing", PaperPluginYaml.Load.OMIT, required = false)
            }
        }
        bukkitPluginYaml {
            name = "MoripaFishing-Integration-WorldLifecycle"
            version = project.version.toString()
            website = "https://fishing.plugin.morino.party"
            main = "party.morino.moripafishing.integrations.worldlifecycle.MoripaFishingWorldLifecyclePlugin"
            apiVersion = "1.20"
            // 旧 bukkit ローダー使用時のフォールバック。softDepend は循環を許容する。
            softDepend.set(listOf("MoripaFishing"))
        }
    }
}
