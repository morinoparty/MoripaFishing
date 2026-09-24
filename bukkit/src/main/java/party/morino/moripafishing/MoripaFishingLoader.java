package party.morino.moripafishing;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * MoripaFishingプラグインのローダークラス。
 * ビルド時に生成したライブラリ一覧（JARに同梱していない依存）を Paper に取得させる。
 *
 * Kotlin標準ライブラリ自体もここで取得するため、このクラスはKotlinではなくJavaで記述する。
 */
@SuppressWarnings("unused")
public class MoripaFishingLoader implements PluginLoader {
    private static final String LIBRARIES_RESOURCE = "paper-libraries.txt";

    // Paper が推奨する Maven Central のミラー（Maven Central を直接参照すると Paper に警告される）
    private static final String MAVEN_CENTRAL_MIRROR = "https://maven-central.storage-download.googleapis.com/maven2";

    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(LIBRARIES_RESOURCE)) {
            if (stream == null) {
                throw new IllegalStateException(LIBRARIES_RESOURCE + " is not found in the plugin jar");
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            reader.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .forEach(coordinate -> resolver.addDependency(new Dependency(new DefaultArtifact(coordinate), null)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        resolver.addRepository(new RemoteRepository.Builder("central", "default", MAVEN_CENTRAL_MIRROR).build());

        classpathBuilder.addLibrary(resolver);
    }
}
