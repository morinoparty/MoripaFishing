package party.morino.moripafishing.utils.coroutines

import org.bukkit.plugin.java.JavaPlugin
import party.morino.moripafishing.MoripaFishing
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.java

object DispatcherContainer {
    private var asyncCoroutine: CoroutineContext? = null
    private var syncCoroutine: CoroutineContext? = null

    /**
     * Gets the async coroutine context.
     */
    val async: CoroutineContext
        get() {
            if (asyncCoroutine == null) {
                asyncCoroutine = AsyncCoroutineDispatcher(JavaPlugin.getPlugin(MoripaFishing::class.java))
            }

            return asyncCoroutine!!
        }

    /**
     * Gets the sync coroutine context.
     */
    val sync: CoroutineContext
        get() {
            if (syncCoroutine == null) {
                syncCoroutine = MinecraftCoroutineDispatcher(JavaPlugin.getPlugin(MoripaFishing::class.java))
            }

            return syncCoroutine!!
        }
}