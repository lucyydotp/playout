package me.lucyydotp.playout.controller

import com.typesafe.config.ConfigFactory
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlin.io.path.Path
import kotlin.io.path.reader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import me.lucyydotp.playout.config.PlayoutConfig
import me.lucyydotp.playout.controller.content.ContentScanner
import me.lucyydotp.playout.controller.env.EnvironmentVariables
import me.lucyydotp.playout.controller.output.OutputManager
import me.lucyydotp.playout.controller.route.casparScannerRoutes
import me.lucyydotp.playout.controller.route.contentRoutes
import me.lucyydotp.playout.controller.route.outputRoutes
import me.lucyydotp.playout.controller.server.amcp.runAmcpServer

private fun Application.module() {
    val config =
        try {
            val hocon = Path(PlayoutConfig.FILE_NAME).reader().use(ConfigFactory::parseReader)
            @OptIn(ExperimentalSerializationApi::class) Hocon.decodeFromConfig<PlayoutConfig>(hocon)
        } catch (ex: Exception) {
            throw RuntimeException("Failed to load config", ex)
        }

    val outputManager = OutputManager(config)
    val content = ContentScanner(config).scan()

    config.amcp
        ?.takeIf { it.enabled }
        ?.let { launch(Dispatchers.IO) { runAmcpServer(outputManager, content, config) } }

    routing {
        localPort(EnvironmentVariables.standalonePort.toInt()) {
            route("/api") {
                get("/healthz") { call.respondText("OK") }
                route("/content") { contentRoutes(content) }
                route("/output") { outputRoutes(outputManager) }
                route("{...}") { handle { call.respond(HttpStatusCode.NotFound) } }
            }

            staticResources("/", "frontend") { default("index.html") }
        }

        EnvironmentVariables.amcpScannerPort?.let {
            log.info("Starting CasparCG scanner API on port $it")
            localPort(it.toInt()) { casparScannerRoutes(content) }
        }
    }
}

/** Sets up a Ktor server to serve controller routes. */
public fun Application.playoutController() {
    install(CallLogging)
    install(ContentNegotiation) { json() }
    install(WebSockets)
    module()
}
