import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.maven.publish)
}

group = "party.morino"
version = project.version.toString()

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates("party.morino", "moripafishing-api", version.toString())

    pom {
        name.set("MoripaFishing API")
        description.set("API for MoripaFishing Minecraft fishing plugin")
        url.set("https://github.com/morinoparty/MoripaFishing")

        licenses {
            license {
                name.set("CC0 1.0 Universal")
                url.set("https://creativecommons.org/publicdomain/zero/1.0/")
            }
        }

        developers {
            developer {
                id.set("morinoparty")
                name.set("MorinoParty")
                url.set("https://github.com/morinoparty")
            }
        }

        scm {
            url.set("https://github.com/morinoparty/MoripaFishing")
            connection.set("scm:git:git://github.com/morinoparty/MoripaFishing.git")
            developerConnection.set("scm:git:ssh://git@github.com/morinoparty/MoripaFishing.git")
        }
    }
}

dependencies {
    // AnglerFishCaughtEvent など公開イベント用の例外的な Bukkit 依存 (実装詳細の持ち込みではない)
    compileOnly(libs.paper.api)

    // @Serializable モデルとカスタムシリアライザ (Key/Component) が公開 ABI に含まれる
    api(libs.kotlinx.serialization.json)
    api(libs.bundles.kyori)

    implementation(libs.kaml)
    implementation(libs.uuid.creator)
}

kotlin {
    jvmToolchain(25)
}

tasks {
    compileKotlin {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_25)
        compilerOptions.javaParameters = true
    }
}
repositories {
    mavenCentral()
}
