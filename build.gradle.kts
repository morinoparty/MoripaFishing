plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.shadow) apply false
    alias(libs.plugins.resource.factory) apply false
    alias(libs.plugins.run.paper) apply false
    alias(libs.plugins.maven.publish) apply false
    // ルートの build.gradle.kts / settings.gradle.kts を検査するため、ルートにも適用する
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.dokka)
    alias(libs.plugins.dokka.javadoc)
}

// Paper プラグインとして配布するモジュール (shadowJar を配布物にする)
val paperPluginModules = setOf(":bukkit", ":integrations:weather", ":integrations:world-lifecycle", ":addons:catch-announce")

// subprojects {} 内の `libs` はサブプロジェクト側のアクセサに解決され、評価前で未登録のため、ルートのカタログを捕捉しておく
val catalog = libs

subprojects {
    if (childProjects.isNotEmpty()) return@subprojects // :addons / :integrations はコンテナ
    group = "party.morino"
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "dev.detekt")
    extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
        jvmToolchain(25)
        compilerOptions.javaParameters = true
    }
    extensions.configure<dev.detekt.gradle.extensions.DetektExtension> {
        buildUponDefaultConfig = true
        allRules = false
        parallel = true
        config.setFrom(rootProject.file("config/detekt/detekt.yml"))
    }
    dependencies {
        "testImplementation"(platform(catalog.junit.bom))
        "testImplementation"(catalog.junit.jupiter)
        "testRuntimeOnly"(catalog.junit.platform.launcher)
        "testImplementation"(catalog.allure.junit5)
    }
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        systemProperty(
            "allure.results.directory",
            layout.buildDirectory
                .dir("allure-results")
                .get()
                .asFile.path,
        )
    }
    tasks.withType<JavaCompile>().configureEach { options.encoding = "UTF-8" }
    if (path in paperPluginModules) {
        apply(plugin = "com.gradleup.shadow")
        dependencies {
            "compileOnly"(catalog.paper.api)
            "testImplementation"(catalog.paper.api)
            "testImplementation"(catalog.mock.bukkit)
        }
        tasks.named<Jar>("jar") { archiveClassifier = "plain" }
        tasks.named<Jar>("shadowJar") { archiveClassifier = "" }
        tasks.named("build") { dependsOn("shadowJar") }
    }
}

dependencies {
    dokka(project(":api"))
}

dokka {
    pluginsConfiguration.html {
        footerMessage.set("No right reserved. This docs under CC0 1.0.")
    }
    dokkaPublications.html {
        outputDirectory.set(file("${project.rootDir}/docs/public/dokka"))
    }
    dokkaPublications.javadoc {
        outputDirectory.set(file("${project.rootDir}/docs/public/javadoc"))
    }
}
