package party.morino.moripafishing.api.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

/**
 * KyoriのComponentとMiniMessageの相互変換を行うユーティリティクラス
 */
object ComponentSerializer : KSerializer<Component> {
    private val miniMessage = MiniMessage.miniMessage()
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Component", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Component) {
        encoder.encodeString(miniMessage.serialize(value))
    }

    override fun deserialize(decoder: Decoder): Component {
        return miniMessage.deserialize(decoder.decodeString())
    }
} 