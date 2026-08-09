package me.lucyydotp.playout.controller

import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import me.lucyydotp.playout.controller.env.EnvironmentVariables

/** Runs a standalone controller server. */
public fun main() {
    embeddedServer(
            Netty,
            configure = {
                listOfNotNull(
                        EnvironmentVariables.standalonePort,
                        EnvironmentVariables.amcpScannerPort,
                    )
                    .forEach {
                        connector {
                            host = "0.0.0.0"
                            port = it.toInt()
                        }
                    }
            },
        ) {
            playoutController()
        }
        .start(true)
}
