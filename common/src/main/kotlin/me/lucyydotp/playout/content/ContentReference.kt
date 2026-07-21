package me.lucyydotp.playout.content

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/** A reference to a known piece of content. */
@Serializable(with = ContentReference.Serializer::class)
public sealed class ContentReference {

    /** The reference's type. */
    public abstract val type: String

    /** Encodes the reference to a string, not including the type prefix. */
    protected abstract fun encode(): String

    /**
     * A solid colour.
     *
     * @param color The ARGB colour to display.
     */
    public data class SolidColor(val color: UInt) : ContentReference() {
        override val type: String = "solid-color"

        /** Encodes the colour as a hex string. */
        override fun encode(): String = "#${color.toString(16).padStart(8, '0')}"
    }

    /** An OGraf graphic. */
    public data class OGraf(val id: String) : ContentReference() {
        override val type: String = "ograf"

        override fun encode(): String = id
    }

    public companion object {
        private const val TYPE_DELIMITER = ':'

        private val SOLID_COLOR_REGEX = Regex("^#([0-9a-fA-F]{6})$")

        /** Parses a [ContentReference] from a string. */
        public fun fromString(string: String): ContentReference {
            require(string.contains(TYPE_DELIMITER)) { "Missing type" }

            val (type, content) = string.split(TYPE_DELIMITER, limit = 2)

            return when (type) {
                "solid-color" -> {
                    require(content.matches(SOLID_COLOR_REGEX)) {
                        "Invalid solid-color reference: $string"
                    }
                    SolidColor(content.substring(1).toUInt(16))
                }

                "ograf" -> OGraf(content)
                else -> throw IllegalArgumentException("Unknown content reference type: $type")
            }
        }
    }

    /** Serializes a [ContentReference] to a string. */
    public object Serializer : KSerializer<ContentReference> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor(ContentReference::class.qualifiedName!!, PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: ContentReference) {
            encoder.encodeString("${value.type}:${value.encode()}")
        }

        override fun deserialize(decoder: Decoder): ContentReference =
            fromString(decoder.decodeString())
    }
}
