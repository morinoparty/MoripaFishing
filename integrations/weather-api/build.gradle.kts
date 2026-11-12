import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    java
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.maven.publish)
}

group = "party.morino"
version = project.version.toString()

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates("party.morino", "moripafishing-weather-api", version.toString())

    pom {
        name.set("MoripaFishing Weather Integration API")
        description.set("SPI for the MoripaFishing weather integration")
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
    // SPI のみを提供するモジュール。`:api` にも Bukkit にも依存しない。
}

tasks {
    compileKotlin {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_25)
        compilerOptions.javaParameters = true
        // 既定実装を持つ SPI メソッドを JVM の default メソッドとして出力する。
        // 後から追加したメソッドを実装していない旧版の Integration でも AbstractMethodError にならないようにするため。
        compilerOptions.jvmDefault.set(JvmDefaultMode.ENABLE)
    }
}

repositories {
    mavenCentral()
}
