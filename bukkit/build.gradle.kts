plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
    alias(libs.plugins.run.paper)
    alias(libs.plugins.resource.factory)
}

dependencies {
    implementation(project(":api"))
    implementation(libs.bundles.commands)
    implementation(libs.exp4j)
}

// JAR に同梱する依存。これ以外の runtimeClasspath の外部依存は MoripaFishingLoader が Paper に実行時に取得させる
fun isBundled(
    group: String,
    version: String,
): Boolean =
    // api モジュール
    group == "party.morino" ||
        // スナップショット版は Maven Central にない
        version.endsWith("-SNAPSHOT")

// runtimeClasspath のうち同梱しない外部依存 (KMP は解決済みの -jvm アーティファクトになる)
val runtimeLibraries =
    configurations.runtimeClasspath.map { configuration ->
        configuration.incoming.artifacts.artifacts
            .mapNotNull { it.id.componentIdentifier as? ModuleComponentIdentifier }
            .filterNot { isBundled(it.group, it.version) }
            .map { "${it.group}:${it.module}:${it.version}" }
            .distinct()
    }

// MoripaFishingLoader が読み込むライブラリ一覧をリソースとして生成する
val generatePaperLibraries =
    tasks.register("generatePaperLibraries") {
        val libraries = runtimeLibraries
        val outputDirectory = layout.buildDirectory.dir("generated/paper-libraries")
        inputs.property("libraries", libraries)
        outputs.dir(outputDirectory)
        doLast {
            outputDirectory
                .get()
                .file("paper-libraries.txt")
                .asFile
                .writeText(libraries.get().joinToString("\n"))
        }
    }

sourceSets.main {
    resources.srcDir(generatePaperLibraries)
}

tasks {
    shadowJar {
        // Paper が実行時に取得するので同梱しない
        dependencies {
            exclude { !isBundled(it.moduleGroup, it.moduleVersion) }
        }
    }
    runServer {
        minecraftVersion("26.2")
        val plugins =
            runPaper.downloadPluginsSpec {
                modrinth("terra", "6.6.5-BETA-bukkit")
            }
        downloadPlugins {
            downloadPlugins.from(plugins)
        }
    }
}

sourceSets.main {
    resourceFactory {
        paperPluginYaml {
            name = rootProject.name
            version = project.version.toString()
            website = "https://fishing.plugin.morino.party"
            main = "$group.moripafishing.MoripaFishing"
            apiVersion = "26.2"
            bootstrapper = "$group.moripafishing.MoripaFishingBootstrap"
            loader = "$group.moripafishing.MoripaFishingLoader"
        }
    }
}
