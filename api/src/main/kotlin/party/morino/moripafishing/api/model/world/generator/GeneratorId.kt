package party.morino.moripafishing.api.model.world.generator

import kotlinx.serialization.Serializable
import party.morino.moripafishing.api.utils.serializer.GeneratorIdSerializer

@Serializable(with = GeneratorIdSerializer::class)
class GeneratorId(
    val value: String,
) {
    override fun toString(): String = "GeneratorId(value='$value')"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GeneratorId) return false
        return value == other.value
    }

    override fun hashCode(): Int = value.hashCode()
}
