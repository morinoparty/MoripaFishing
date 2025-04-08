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
}



tasks {
    build {
        dependsOn("shadowJar")
    }
    shadowJar
    runServer {
        minecraftVersion("1.21.4")
        val plugins = runPaper.downloadPluginsSpec {
            //Vault
            url("https://github.com/MilkBowl/Vault/releases/download/1.7.3/Vault.jar")
        }
        downloadPlugins {
            downloadPlugins.from(plugins)
        }
    }
}


sourceSets.main {
    resourceFactory {
        bukkitPluginYaml {
            name = rootProject.name
            version = project.version.toString()
            website = "https://fishing.plugin.morino.party"
            main = "$group.moripafishing.MoripaFishing"
            apiVersion = "1.20"
            libraries = libs.bundles.coroutines.asString()
            depend = listOf("Vault")
        }
    }
}

fun Provider<MinimalExternalModuleDependency>.asString(): String {
    val dependency = this.get()
    return dependency.module.toString() + ":" + dependency.versionConstraint.toString()
}

fun Provider<ExternalModuleDependencyBundle>.asString(): List<String> {
    return this.get().map { dependency ->
        "${dependency.group}:${dependency.name}:${dependency.version}"
    }
}
