package party.morino.moripafishing

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkerExecutor
import java.io.File
import java.net.URLClassLoader

abstract class GenerateCommandListTask : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val classesDirectories: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val runtimeClasspath: ConfigurableFileCollection

    @TaskAction
    fun runTask() {
        val classpathUrls = (classesDirectories.files + runtimeClasspath.files).map { it.toURI().toURL() }.toTypedArray()
        val classLoader = URLClassLoader(classpathUrls, null)

        println("Created ClassLoader for reflection with ${classpathUrls.size} URLs.")

        val prefix = "party.morino.moripafishing.ui.commands"

        val foundClasses = getClasses(classLoader, prefix)

        println("Found ${foundClasses.size} classes with prefix: $prefix")
        foundClasses.forEach { println("$it") }
    }

    /**
     * 指定されたクラスローダーとプレフィックスを使って、クラスを取得するのだ
     * @param classLoader クラスをロードするためのClassLoader
     * @param prefix 検索対象のクラスのパッケージプレフィックス
     * @return プレフィックスに一致するクラスの完全修飾名のリスト
     */
    private fun getClasses(classLoader: URLClassLoader, prefix: String): List<String> {
        val foundClasses = mutableListOf<String>()

        // prefixで始まるクラスファイルだけをフィルタリングするのだ
        classesDirectories.asFileTree.filter { file ->
            file.isFile && 
            file.name.endsWith(".class") && 
            calculateClassName(file)?.startsWith(prefix) == true
        }.forEach { classFile ->
            val className = calculateClassName(classFile) ?: return@forEach

            try {
                val loadedClass = classLoader.loadClass(className).kotlin
                val qualifiedName = loadedClass.qualifiedName
                if (qualifiedName != null) {
                    foundClasses.add(qualifiedName)
                }
            } catch (e: ClassNotFoundException) {
                logger.warn("Could not load class: $className (${e.message})")
            } catch (e: NoClassDefFoundError) {
                logger.warn("Missing dependency for class: $className (${e.message})")
            } catch (t: Throwable) {
                logger.error("Error reflecting on class: $className", t)
            }
        }

        return foundClasses
    }

    private fun calculateClassName(classFile: File): String? {
        val rootDir = classesDirectories.files.find { classFile.absolutePath.startsWith(it.absolutePath + File.separator) } ?: return null
        val relativePath = classFile.absolutePath.substring(rootDir.absolutePath.length + 1)
        return relativePath.removeSuffix(".class").replace(File.separatorChar, '.')
    }
}
