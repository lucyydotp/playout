package me.lucyydotp.playout.content.ograf

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * An OGraf manifest.
 *
 * https://ograf.ebu.io/v1/specification/docs/Specification.html
 */
@Serializable
public data class OGrafManifest(
    public val id: String,
    public val name: String,
    public val main: String,
    public val supportsRealTime: Boolean,
    public val supportsNonRealTime: Boolean,
    public val schema: JsonObject? = null,
    @SerialName($$"$schema") private val jsonSchema: String? = null,
) {
    public companion object {
        public const val EXPECTED_SCHEMA: String =
            "https://ograf.ebu.io/v1/specification/json-schemas/graphics/schema.json"
    }

    init {
        require(jsonSchema == EXPECTED_SCHEMA) { "Invalid schema: $jsonSchema" }
        require(supportsRealTime) { "OGraf graphic must support real-time playback" }
    }
}
