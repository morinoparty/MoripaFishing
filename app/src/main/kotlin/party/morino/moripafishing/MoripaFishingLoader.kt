package party.morino.moripafishing

import io.papermc.paper.plugin.loader.PluginLoader
import io.papermc.paper.plugin.loader.PluginClasspathBuilder
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.repository.RemoteRepository

/**
 * MoripaFishingプラグインのローダークラス
 * プラグインの依存関係を管理するためのクラス
 */
@Suppress("unused")
class MoripaFishingLoader : PluginLoader {
    override fun classloader(classpathBuilder: PluginClasspathBuilder) {
        
        // 依存関係の追加
        val resolver = MavenLibraryResolver()
        resolver.addDependency(Dependency(DefaultArtifact("org.jetbrains.kotlin:kotlin-stdlib:1.9.0"), null))
        resolver.addRepository(RemoteRepository.Builder("paper", "default", "https://repo.papermc.io/repository/maven-public/").build())
        
        classpathBuilder.addLibrary(resolver)
    }
} 