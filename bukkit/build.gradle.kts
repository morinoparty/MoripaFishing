import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import party.morino.moripafishing.GenerateCommandListTask
import xyz.jpenilla.resourcefactory.paper.PaperPluginYaml

plugins {
    java
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
    alias(libs.plugins.run.paper)
    alias(libs.plugins.resource.factory)
}

group = "party.morino"
version = project.version.toString()

dependencies {
    implementation(project(":api"))
    // WorldLifecycle Integration の SPI。
    // core 自身は shade せず、integration plugin (softdepend) がロードした
    // `WorldLifecycleProvider` クラスを `join-classpath: true` 経由で参照する。
    compileOnly(project(":integrations:world-lifecycle-api"))
    // Weather Integration の SPI。world-lifecycle 同様に core では shade せず、
    // integration plugin (softdepend) がロードした `WeatherControlProvider` を参照する。
    compileOnly(project(":integrations:weather-api"))

    compileOnly(libs.paper.api)

    implementation(libs.arrow.core)
    implementation(libs.arrow.fx.coroutines)

    implementation(libs.bundles.commands)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kaml)

    implementation(libs.bundles.coroutines)

    implementation(libs.bundles.database)

    implementation(libs.koin.core)

    implementation(libs.uuid.creator)

    implementation(libs.bundles.kyori)

    implementation(libs.exp4j)

    implementation(libs.noise)

    testImplementation(libs.paper.api)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.mock.bukkit)
    // テスト時のみ、MockBukkit が MoripaFishing を ByteBuddy でプロキシする際に
    // `WorldLifecycleProvider` / `WeatherControlProvider` の型解決が必要なので含める。
    // production jar には同梱されない。
    testImplementation(project(":integrations:world-lifecycle-api"))
    testImplementation(project(":integrations:weather-api"))

    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation(libs.bundles.junit.jupiter)
    testImplementation(libs.bundles.koin.test)
    testImplementation(libs.junit.platform.launcher)
    testImplementation(libs.allure.junit5)
}

// JARに同梱する依存。これ以外のruntimeClasspathの外部依存はPaperに実行時に取得させる
// （paper-plugin.yml では MoripaFishingLoader、plugin.yml では libraries を使う）
fun isBundled(
    group: String,
    version: String,
): Boolean =
    // apiモジュール
    group == "party.morino" ||
        // Adventure / MiniMessage は従来どおり同梱する（Paper本体が提供するものが優先される）
        group == "net.kyori" ||
        // スナップショット版はMaven Centralにない
        version.endsWith("-SNAPSHOT")

// runtimeClasspathのうち同梱しない外部依存（KMPは解決済みの -jvm アーティファクトになる）
val runtimeLibraries =
    configurations.runtimeClasspath.map { configuration ->
        configuration.incoming.artifacts.artifacts
            .mapNotNull { it.id.componentIdentifier as? ModuleComponentIdentifier }
            .filterNot { isBundled(it.group, it.version) }
            .map { "${it.group}:${it.module}:${it.version}" }
            .distinct()
    }

// MoripaFishingLoader が読み込むライブラリ一覧をリソースとして生成する
val generatePaperLibraries by tasks.registering {
    val libraries = runtimeLibraries
    val outputDirectory = layout.buildDirectory.dir("generated/paper-libraries")
    inputs.property("libraries", libraries)
    outputs.dir(outputDirectory)
    doLast {
        outputDirectory.get().file("paper-libraries.txt").asFile.writeText(libraries.get().joinToString("\n"))
    }
}

sourceSets.main {
    resources.srcDir(generatePaperLibraries)
}

tasks {
    build {
        dependsOn("shadowJar")
    }
    test {
        useJUnitPlatform()
        // Allure結果の出力先を指定
        systemProperty("allure.results.directory", "${project.layout.buildDirectory.get().asFile}/allure-results")
        testLogging {
            showStandardStreams = true
            events("passed", "skipped", "failed")
            exceptionFormat = TestExceptionFormat.FULL
        }
    }
    shadowJar {
        // Paperが実行時に取得するので同梱しない
        dependencies {
            exclude { !isBundled(it.moduleGroup, it.moduleVersion) }
        }
    }
    runServer {
        minecraftVersion("26.1.2")
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
            apiVersion = "1.20"
            bootstrapper = "$group.moripafishing.MoripaFishingBootstrap"
            loader = "$group.moripafishing.MoripaFishingLoader"
            dependencies {
                server("MoripaFishing-Integration-WorldLifecycle", PaperPluginYaml.Load.BEFORE, required = false)
                server("MoripaFishing-Integration-Weather", PaperPluginYaml.Load.BEFORE, required = false)
            }
        }
        bukkitPluginYaml {
            name = rootProject.name
            version = project.version.toString()
            website = "https://fishing.plugin.morino.party"
            main = "$group.moripafishing.MoripaFishing"
            apiVersion = "1.20"
            softDepend.set(listOf("MoripaFishing-Integration-WorldLifecycle", "MoripaFishing-Integration-Weather"))
            // Spigot など paper-plugin.yml を使わないサーバー向け
            libraries.set(runtimeLibraries)
        }
    }
}

tasks.register<GenerateCommandListTask>("generateCommandList") {
    // タスクの説明
    description = "Detects classes annotated with @Command using reflection."
    // タスクのグループ
    group = "verification"

    // appモジュールのメインソースセットを取得
    val mainSourceSet = sourceSets.main.get()

    // タスクの入力プロパティに値を設定する
    // コンパイルされたクラスが出力されるディレクトリを指定
    classesDirectories.from(mainSourceSet.output.classesDirs)
    // 実行に必要な依存関係を含むクラスパスを指定
    runtimeClasspath.from(mainSourceSet.runtimeClasspath)
}
