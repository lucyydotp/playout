package me.lucyydotp.playout.controller.output

import me.lucyydotp.playout.config.PlayoutConfig

/** Manages currently active outputs. */
public class OutputManager(private val config: PlayoutConfig) {

    private val outputs = config.outputs.mapValues { Output(it.key, it.value) }

    /** A map of outputs that have ACMP channels configured. */
    public val byAcmpId: Map<Int, Output> =
        outputs.values
            .filter { it.config.acmpChannel != null }
            .associateBy { it.config.acmpChannel!! }

    /** Gets an output by name, or null if it doesn't exist. */
    public operator fun get(channel: String): Output? = outputs[channel]

    /** Gets a map of every output. */
    public fun all(): Map<String, Output> = outputs
}
