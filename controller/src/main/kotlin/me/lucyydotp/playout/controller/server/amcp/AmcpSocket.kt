package me.lucyydotp.playout.controller.server.amcp

import me.lucyydotp.playout.controller.output.OutputManager

/**
 * Handles AMCP commands.
 * A subset of CasparCG commands are supported. See https://casparcg.com/docs/wiki/protocols/amcp-protocol.
 */
public class AmcpSocket(public val outputManager: OutputManager) {

    private val commandTree = CommandTree {
        "PING" { (listOf("PONG") + it).joinToString(" ") }
    }

    /**
     * Handles an AMCP command.
     * @param command the raw command received from the client
     * @return the response to send to the client
     */
    public fun handle(command: String): String {
        // TODO: work out the exact format needed here with newlines etc
        val parsed = try {
            splitCommand(command)
        } catch (ex: AmcpCommandParseException) {
            return "400 ERROR ${ex.message}\r\n"
        }

        val (node, args) = commandTree.find(parsed) ?: return "404 ERROR Command not found\r\n"
        return node.handle(args)
    }
}
