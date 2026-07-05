package me.lucyydotp.playout.controller.server.amcp

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import me.lucyydotp.playout.content.ContentReference
import me.lucyydotp.playout.controller.output.OutputManager
import org.slf4j.LoggerFactory

/**
 * Handles AMCP commands. A subset of CasparCG commands are supported. See
 * https://casparcg.com/docs/wiki/protocols/amcp-protocol.
 */
public class AmcpSocket(public val outputManager: OutputManager) {

    private companion object {
        private val logger = LoggerFactory.getLogger(AmcpSocket::class.java)

        private const val OK = "202 OK\r\n"
    }

    private fun CommandContext.cgChannel() =
        parseChannelAndLayer(wildcardValues.first()).let {
            (outputManager[it.first.toString()]
                ?: throw IllegalStateException("Invalid channel")) to it.second
        }

    private val commandTree = CommandTree {
        "PING" { (listOf("PONG") + it.arguments).joinToString(" ") }

        "TLS" { TODO("Content catalogue") }

        "CG * ADD" {
            // CG [video_channel:int]{-[layer:int]|-9999} ADD [cg_layer:int] [template:string]
            // [play-on-load:0,1] {[data]}
            val (channel, layer) = it.cgChannel()
            val (_, template, playOnLoad) = it.arguments
            val data =
                it.arguments.getOrNull(3)?.let { json ->
                    Json.parseToJsonElement(json) as JsonObject
                }

            // TODO: parse content ref properly
            channel.load(layer, ContentReference.OGraf(template), data ?: JsonObject(emptyMap()))
            if (playOnLoad == "1") channel.play(layer)
            OK
        }

        "CG * PLAY" {
            // CG [video_channel:int]{-[layer:int]|-9999} PLAY [cg_layer:int]
            val (channel, layer) = it.cgChannel()
            try {
                channel.play(layer)
                OK
            } catch (_: NoSuchElementException) {
                "403 CG ERROR\r\n" // TODO: check the format of this
            }
        }

        "CG * STOP" {
            // CG [video_channel:int]{-[layer:int]|-9999} STOP [cg_layer:int]
            val (channel, layer) = it.cgChannel()
            try {
                channel.stop(layer)
                OK
            } catch (_: NoSuchElementException) {
                "403 CG ERROR\r\n" // TODO: check the format of this
            }
        }
    }

    /**
     * Handles an AMCP command.
     *
     * @param command the raw command received from the client
     * @return the response to send to the client
     */
    public fun handle(command: String): String {
        val parsed =
            try {
                splitCommand(command)
            } catch (ex: AmcpCommandParseException) {
                logger.warn("Failed to parse ACMP command `${command}`", ex)
                return "400 ERROR\r\n${command}\r\n"
            }

        val context =
            try {
                commandTree.find(parsed) ?: return "404 ERROR\r\n${command}\r\n"
            } catch (ex: AmcpCommandParseException) {
                logger.warn("Failed to parse ACMP command `${command}`", ex)
                return "400 ERROR\r\n${command}\r\n"
            }
        return context()
    }
}
