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
    compileOnly(project(":api"))
    compileOnly(libs.paper.api)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.koin.core)

    testImplementation(project(":api"))
    testImplementation(libs.paper.api)
    testImplementation(libs.mock.bukkit)
    testImplementation(platform("org.junit:junit-bom:6.0.3"))
    testImplementation(libs.bundles.junit.jupiter)
    testImplementation(libs.junit.platform.launcher)
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
    }
}

sourceSets.main {
    resourceFactory {
        paperPluginYaml {
            name = "MoripaFishingWorldLifecycle"
            version = project.version.toString()
            website = "https://fishing.plugin.morino.party"
            main = "party.morino.moripafishing.integrations.worldlifecycle.MoripaFishingWorldLifecyclePlugin"
            apiVersion = "1.20"
        }
        bukkitPluginYaml {
            name = "MoripaFishingWorldLifecycle"
            version = project.version.toString()
            website = "https://fishing.plugin.morino.party"
            main = "party.morino.moripafishing.integrations.worldlifecycle.MoripaFishingWorldLifecyclePlugin"
            apiVersion = "1.20"
        }
    }
}
