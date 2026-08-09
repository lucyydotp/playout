package me.lucyydotp.playout.controller.route

import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import me.lucyydotp.playout.controller.content.ScannedContent

@Serializable
private data class MediaScannerTemplate(
    val id: String,
    val path: String,
    val type: String,
    val gdd: JsonObject? = null,
)

@Serializable private data class TemplatesResponse(val templates: List<MediaScannerTemplate>)

/** Sets up routes for the CasparCG scanner-compatible API. */
public fun Route.casparScannerRoutes(content: ScannedContent) {
    get("/templates") {
        call.respond(
            TemplatesResponse(
                content.ograf.map { (id, graphic) ->
                    MediaScannerTemplate(
                        "ograf:$id",
                        graphic.manifestPath.toString(),
                        "html",
                        gdd = graphic.manifest.schema,
                    )
                }
            )
        )
    }
}
