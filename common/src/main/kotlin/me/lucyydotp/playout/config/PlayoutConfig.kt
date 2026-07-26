package me.lucyydotp.playout.config

import kotlinx.serialization.Serializable
import me.lucyydotp.playout.model.Resolution
import me.lucyydotp.playout.serialization.SerializablePath

/** The config file for a playout controller. */
@Serializable
public data class PlayoutConfig(
    public val outputs: Map<String, OutputConfig>,
    public val content: ContentConfig,
    public val amcp: AmcpConfig? = null,
) {
    public companion object {
        public const val FILE_NAME: String = "playout.conf"
    }
}

/** Configuration for the AMCP server. */
@Serializable
public data class AmcpConfig(
    /** Whether the AMCP server is enabled. */
    val enabled: Boolean = false,
    /** The port to run the AMCP server on. */
    val port: UShort = 5250u,
)

/** Configuration for content. */
@Serializable
public data class ContentConfig(
    /** The path to serve content from. */
    val path: SerializablePath
)

/** Configuration for a specific output. */
@Serializable
public data class OutputConfig(
    /** The output's resolution. */
    val resolution: Resolution,
    /**
     * The channel this output can be accessed on via CasparCG-compatible ACMP. If not configured,
     * this output will not be accessible via ACMP.
     */
    val acmpChannel: Int? = null,
)
