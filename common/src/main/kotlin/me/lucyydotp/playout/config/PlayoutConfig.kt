package me.lucyydotp.playout.config

import kotlinx.serialization.Serializable
import me.lucyydotp.playout.model.Resolution
import me.lucyydotp.playout.serialization.SerializablePath

/** The config file for a playout controller. */
@Serializable
public data class PlayoutConfig(
    public val outputs: Map<String, OutputConfig>,
    public val content: ContentConfig,
) {
    public companion object {
        public const val FILE_NAME: String = "playout.conf"
    }
}

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
    val resolution: Resolution
)
