package me.lucyydotp.playout.controller

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

/** Runs a standalone controller server. */
public fun main() {
    embeddedServer(Netty) {}
}
