package me.lucyydotp.playout.controller.server.amcp

import kotlin.collections.map
import me.lucyydotp.playout.config.PlayoutConfig
import me.lucyydotp.playout.content.ContentReference
import me.lucyydotp.playout.controller.content.ScannedContent
import me.lucyydotp.playout.controller.output.OutputManager
import me.lucyydotp.playout.controller.server.amcp.AmcpCommand.Info.InfoType

/** Handles [AmcpCommand]s. */
public class AmcpCommandDispatcher(
    private val outputManager: OutputManager,
    private val content: ScannedContent,
    private val config: PlayoutConfig,
) {
    /**
     * Dispatches a command.
     *
     * @param command the command to dispatch
     * @return a message to respond to the client with
     */
    public fun dispatch(command: AmcpCommand): String =
        when (command) {
            is AmcpCommand.Ping -> (listOf("PONG") + command.args).joinToString(" ")
            is AmcpCommand.Cls -> "200 CLS OK\r\n\r\n"
            is AmcpCommand.Tls -> {

                val refs =
                    buildList<ContentReference> {
                        content.ograf.mapTo(this) { ContentReference.OGraf(it.key) }
                    }

                "200 TLS OK\r\n${refs.joinToString("\r\n")}\r\n\r\n"
            } // TODO: properly handle subpaths
            is AmcpCommand.Version -> "201 VERSION OK\r\n0.1.0\r\n"

            is AmcpCommand.Info ->
                when (command.type) {
                    InfoType.Channels ->
                        "200 INFO OK\r\n${
                outputManager.all()
                    .map { "${it.key} ${it.value.config.resolution.height}p6000 PLAYING\r\n" }
                    .joinToString("")
            }\r\n"

                    InfoType.Config -> "201 INFO CONFIG OK\r\n${config.asCasparCompatibleXml()}\r\n"
                }

            is AmcpCommand.CgAdd -> {
                val output = outputManager.byAcmpId[command.channel] ?: return "403 CG ERROR\r\n"
                output.load(command.layer, command.template, command.templateData)
                if (command.playOnLoad) output.play(command.layer)
                "202 CG OK\r\n"
            }

            is AmcpCommand.CgPlay -> {
                val output = outputManager.byAcmpId[command.channel] ?: return "403 CG ERROR\r\n"
                try {
                    output.play(command.layer)
                    "202 CG OK\r\n"
                } catch (_: NoSuchElementException) {
                    "403 CG ERROR\r\n"
                }
            }

            is AmcpCommand.CgUpdate -> {
                val output = outputManager.byAcmpId[command.channel] ?: return "403 CG ERROR\r\n"
                try {
                    output.update(command.layer, command.templateData)
                    "202 CG OK\r\n"
                } catch (_: NoSuchElementException) {
                    "403 CG ERROR\r\n"
                }
            }

            is AmcpCommand.CgStop -> {
                val output = outputManager.byAcmpId[command.channel] ?: return "403 CG ERROR\r\n"
                try {
                    output.stop(command.layer)
                    "202 CG OK\r\n"
                } catch (_: NoSuchElementException) {
                    "403 CG ERROR\r\n"
                }
            }
        }
}
