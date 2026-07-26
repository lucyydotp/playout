package me.lucyydotp.playout.controller

import com.typesafe.config.ConfigFactory
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import kotlin.io.path.Path
import kotlin.io.path.reader
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import me.lucyydotp.playout.config.PlayoutConfig
import me.lucyydotp.playout.controller.content.ContentScanner
import me.lucyydotp.playout.controller.output.OutputManager
import me.lucyydotp.playout.controller.route.contentRoutes
import me.lucyydotp.playout.controller.route.outputRoutes

private suspend fun Application.module() {
    val config =
        try {
            val hocon = Path(PlayoutConfig.FILE_NAME).reader().use(ConfigFactory::parseReader)
            @OptIn(ExperimentalSerializationApi::class) Hocon.decodeFromConfig<PlayoutConfig>(hocon)
        } catch (ex: Exception) {
            throw RuntimeException("Failed to load config", ex)
        }

    val outputManager = OutputManager(config)
    val content = ContentScanner(config).scan()

    routing {
        route("/api") {
            get("/healthz") { call.respondText("OK") }
            route("/content") { contentRoutes(content) }
            route("/output") { outputRoutes(outputManager) }
        }
    }
}

/** Runs a standalone controller server. */
public fun main() {
    embeddedServer(Netty, port = 8080) {
            install(ContentNegotiation) { json() }
            install(WebSockets)
            module()
        }
        .start(true)
}
