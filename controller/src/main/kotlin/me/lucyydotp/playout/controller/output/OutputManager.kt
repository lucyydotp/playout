package me.lucyydotp.playout.controller.output

import me.lucyydotp.playout.config.PlayoutConfig

/** Manages currently active outputs. */
public class OutputManager(private val config: PlayoutConfig) {

    private val outputs = config.outputs.mapValues { Output(it.key, it.value) }

    /** Gets an output by name, or null if it doesn't exist. */
    public operator fun get(channel: String): Output? = outputs[channel]
}
