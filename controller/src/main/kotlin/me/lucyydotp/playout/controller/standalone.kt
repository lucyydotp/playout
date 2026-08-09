package me.lucyydotp.playout.controller

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import me.lucyydotp.playout.controller.env.EnvironmentVariables

/** Runs a standalone controller server. */
public fun main() {
    embeddedServer(Netty, port = EnvironmentVariables.standalonePort.toInt()) {
            playoutController()
        }
        .start(true)
}
