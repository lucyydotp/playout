package me.lucyydotp.playout.controller.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import me.lucyydotp.playout.config.OutputConfig
import me.lucyydotp.playout.content.ContentReference
import me.lucyydotp.playout.controller.output.OutputManager
import me.lucyydotp.playout.state.LayerState

@Serializable
private data class LoadPayload(
    val layer: Int,
    val content: ContentReference,
    val templateData: JsonObject? = null,
)

@Serializable
private data class OutputState(val config: OutputConfig, val layers: Map<Int, LayerState>)

/** Sets up routes for controlling outputs. */
public fun Route.outputRoutes(outputManager: OutputManager) {

    post("/{output}/load") {
        val payload = call.receive<LoadPayload>()

        val output =
            outputManager[call.parameters["output"]!!]
                ?: return@post call.respond(HttpStatusCode.NotFound, "Unknown output")

        output.load(payload.layer, payload.content, payload.templateData ?: JsonObject(emptyMap()))
        call.respond(HttpStatusCode.OK)
    }

    post("/{output}/play/{layer}") {
        val output =
            outputManager[call.parameters["output"]!!]
                ?: return@post call.respond(HttpStatusCode.NotFound, "Unknown output")

        val layer =
            call.parameters["layer"]?.toIntOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, "Layer must be an integer")

        output.play(layer)
        call.respond(HttpStatusCode.OK)
    }

    webSocket("/{output}/watch") {
        val output = outputManager[call.parameters["output"]!!]
        if (output == null) {
            close(
                CloseReason(
                    CloseReason.Codes.VIOLATED_POLICY,
                    "Unknown output ${call.parameters["output"]}",
                )
            )
            return@webSocket
        }

        output.state
            .map { OutputState(output.config, it) }
            .collect { send(Frame.Text(Json.encodeToString(it))) }
    }
}
