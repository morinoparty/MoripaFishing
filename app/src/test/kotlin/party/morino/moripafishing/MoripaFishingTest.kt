package party.morino.moripafishing

import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import party.morino.moripafishing.api.config.ConfigManager
import party.morino.moripafishing.config.ConfigManagerMock

class MoripaFishingTest: BeforeEachCallback, AfterEachCallback {


    override fun beforeEach(context: ExtensionContext) {
        println("beforeEach() executed before " + context.displayName + ".");
        setupKoin()
    }

    override fun afterEach(context: ExtensionContext) {
        stopKoin()
    }

    private fun setupKoin() {
        val module = module {
            single<ConfigManager> { ConfigManagerMock() }
        }
        
        GlobalContext.getOrNull()?.let {
            loadKoinModules(module)
        } ?: GlobalContext.startKoin {
            modules(module)
        }
    }
}