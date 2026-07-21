package me.lucyydotp.playout.content.ograf

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    @SerialName($$"$schema") private val schema: String? = null,
) {
    private companion object {
        private const val EXPECTED_SCHEMA =
            "https://ograf.ebu.io/v1/specification/json-schemas/graphics/schema.json"
    }

    init {
        require(schema == EXPECTED_SCHEMA) { "Invalid schema: $schema" }
    }
}
