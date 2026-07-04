package me.lucyydotp.playout.config

import kotlinx.serialization.Serializable
import me.lucyydotp.playout.model.Resolution

/** The config file for a playout controller. */
@Serializable public data class PlayoutConfig(public val outputs: Map<String, OutputConfig>)

/** Configuration for a specific output. */
@Serializable
public data class OutputConfig(
    /** The output's resolution. */
    val resolution: Resolution
)
