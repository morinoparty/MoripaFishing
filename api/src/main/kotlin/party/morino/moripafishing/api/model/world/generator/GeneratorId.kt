package party.morino.moripafishing.api.model.world.generator

import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import party.morino.moripafishing.api.core.world.GeneratorManager
import party.morino.moripafishing.api.utils.serializer.GeneratorIdSerializer

@Serializable(with = GeneratorIdSerializer::class)
class GeneratorId(val value: String) : KoinComponent {
    private val generatorManager: GeneratorManager by inject()

    override fun toString(): String {
        return "GeneratorId(value='$value')"
    }

    fun toGeneratorData(): GeneratorData {
        return generatorManager.getGenerator(this) ?: throw IllegalArgumentException("Generator not found: $value")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GeneratorId) return false
        return value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}
