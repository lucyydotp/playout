package me.lucyydotp.playout.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/** A display resolution. */
@Serializable(with = Resolution.Serializer::class)
public data class Resolution(
    /** The display's width in pixels. */
    val width: UInt,
    /** The display's height in pixels. */
    val height: UInt,
) {

    init {
        require(width > 0u) { "Width must be positive" }
        require(height > 0u) { "Height must be positive" }
    }

    public object Serializer : KSerializer<Resolution> {

        private val pattern = Regex("(\\d+)x(\\d+)")

        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor(Resolution::class.qualifiedName!!, PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: Resolution) {
            encoder.encodeString("${value.width}x${value.height}")
        }

        override fun deserialize(decoder: Decoder): Resolution {
            val match =
                pattern.matchEntire(decoder.decodeString())
                    ?: throw SerializationException("Invalid resolution format")

            return try {
                Resolution(match.groupValues[1].toUInt(), match.groupValues[2].toUInt())
            } catch (e: IllegalArgumentException) {
                throw SerializationException(e)
            }
        }
    }
}
