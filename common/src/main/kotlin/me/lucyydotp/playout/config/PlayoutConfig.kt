package me.lucyydotp.playout.config

import java.nio.file.Path
import kotlinx.serialization.Serializable
import me.lucyydotp.playout.model.Resolution

/** The config file for a playout controller. */
@Serializable
public data class PlayoutConfig(
    public val outputs: Map<String, OutputConfig>,
    public val content: ContentConfig,
)

/** Configuration for content. */
@Serializable
public data class ContentConfig(
    /** The path to serve content from. */
    val path: Path
)

/** Configuration for a specific output. */
@Serializable
public data class OutputConfig(
    /** The output's resolution. */
    val resolution: Resolution
)
