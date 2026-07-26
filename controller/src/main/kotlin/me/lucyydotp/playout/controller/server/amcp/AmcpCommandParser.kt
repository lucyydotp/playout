package me.lucyydotp.playout.controller.server.amcp

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import me.lucyydotp.playout.content.ContentReference
import me.lucyydotp.playout.util.EMPTY

/** Parses raw AMCP commands into [AmcpCommand]s. */
public object AmcpCommandParser {
    private const val REQ_PREFIX = "REQ"

    private val commandTree = CommandTree {
        "PING" { AmcpCommand.Ping(it.arguments) }

        "TLS" { AmcpCommand.Tls(it.arguments.firstOrNull()) }

        "CLS" { AmcpCommand.Cls }

        "INFO" {
            AmcpCommand.Info(
                it.arguments.firstOrNull()?.let(AmcpCommand.Info.InfoType::get)
                    ?: AmcpCommand.Info.InfoType.Channels
            )
        }

        "VERSION" { AmcpCommand.Version }

        "CG * ADD" {
            // CG [video_channel:int]{-[layer:int]|-9999} ADD [cg_layer:int] [template:string]
            // [play-on-load:0,1] {[data]}
            val (channel, layer) = parseChannelAndLayer(it.wildcardValues.first())
            val (_, template, playOnLoad) = it.arguments
            val data =
                it.arguments.getOrNull(3)?.let { json ->
                    Json.parseToJsonElement(json) as JsonObject
                }

            AmcpCommand.CgAdd(
                channel,
                layer,
                try {
                    ContentReference.fromString(template)
                } catch (ex: IllegalArgumentException) {
                    throw AmcpCommandParseException("Invalid content reference", template, ex)
                },
                playOnLoad == "1",
                data ?: JsonObject.EMPTY,
            )
        }

        "CG * PLAY" {
            // CG [video_channel:int]{-[layer:int]|-9999} PLAY [cg_layer:int]
            val (channel, layer) = parseChannelAndLayer(it.wildcardValues.first())
            AmcpCommand.CgPlay(channel, layer)
        }

        "CG * UPDATE" {
            // CG [video_channel:int]{-[layer:int]|-9999} UPDATE [cg_layer:int] [data:string]
            val (channel, layer) = parseChannelAndLayer(it.wildcardValues.first())
            val (_, data) = it.arguments
            val json = Json.parseToJsonElement(data) as JsonObject
            AmcpCommand.CgUpdate(channel, layer, json)
        }

        "CG * STOP" {
            // CG [video_channel:int]{-[layer:int]|-9999} STOP [cg_layer:int]
            val (channel, layer) = parseChannelAndLayer(it.wildcardValues.first())
            AmcpCommand.CgStop(channel, layer)
        }
    }

    /**
     * Parses an AMCP command.
     *
     * @param command the raw command received from the client
     * @return the parsed command
     * @throws AmcpCommandParseException if the command is invalid
     */
    public fun parse(command: String): ParsedAmcpCommand {
        val split = splitCommand(command)

        // If the first part is REQ then there's an ID, so find it.
        val commandId =
            if (split.first() == REQ_PREFIX) {
                split[1]
            } else null

        val parsed =
            commandTree.find(if (commandId != null) split.drop(2) else split)?.invoke()
                ?: throw AmcpCommandParseException("Unknown command", command)

        return ParsedAmcpCommand(parsed, commandId)
    }
}
