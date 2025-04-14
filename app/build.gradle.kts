import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.kotlin.dsl.testRuntimeOnly
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

    compileOnly(libs.paper.api)

    implementation(libs.arrow.core)
    implementation(libs.arrow.fx.coroutines)

    implementation(libs.bundles.commands)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kaml)

    implementation(libs.bundles.coroutines)

    implementation(libs.bundles.database)

    implementation(libs.koin.core)

    compileOnly(libs.vault.api)

    implementation(libs.bundles.kyori)

    implementation(libs.exp4j)

    implementation(libs.noise)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)

    testImplementation(platform("org.junit:junit-bom:5.12.2"))
    testImplementation(libs.bundles.junit.jupiter)
    testImplementation(libs.bundles.koin.test)
    testImplementation(libs.junit.platform.launcher)

}

tasks {
    build {
        dependsOn("shadowJar")
    }
    test {
        useJUnitPlatform()
        testLogging {
            showStandardStreams = true
            events("passed", "skipped", "failed")
            exceptionFormat = TestExceptionFormat.FULL
        }
    }
    shadowJar
    runServer {
        minecraftVersion("1.21.4")
        val plugins = runPaper.downloadPluginsSpec {
            //Vault
            url("https://github.com/MilkBowl/Vault/releases/download/1.7.3/Vault.jar")
            modrinth("terra", "6.6.1-BETA-bukkit")
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
                server("Vault", PaperPluginYaml.Load.BEFORE)
            }
        }
    }
}
