import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.maven.publish)
}

group = "party.morino"
version = project.version.toString()

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
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
    implementation(libs.arrow.core)
    implementation(libs.arrow.fx.coroutines)

    implementation(libs.koin.core)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kaml)
    implementation(libs.bundles.kyori)

    implementation(libs.uuid.creator)
}

kotlin {
    jvmToolchain {
        (this).languageVersion.set(JavaLanguageVersion.of(21))
    }
    jvmToolchain(21)
}

tasks {
    compileKotlin {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
        compilerOptions.javaParameters = true
    }
}
repositories {
    mavenCentral()
}
