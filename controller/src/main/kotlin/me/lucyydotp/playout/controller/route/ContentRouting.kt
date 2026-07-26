package me.lucyydotp.playout.controller.route

import io.ktor.server.http.content.*
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import me.lucyydotp.playout.controller.content.ScannedContent

/** Sets up routes for the scanned content. */
public fun Route.contentRoutes(content: ScannedContent) {
    route("/ograf") {
        get { call.respond(content.ograf.keys) }

        content.ograf.forEach { (id, graphic) ->
            get(id) { call.respond(graphic.manifest) }

            staticZip(
                remotePath = id,
                basePath = graphic.manifestPath.parent.toString(),
                zip = graphic.archivePath,
                index = null,
            )
        }
    }
}
