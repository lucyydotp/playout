package me.lucyydotp.playout.controller.server.amcp

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import me.lucyydotp.playout.config.PlayoutConfig
import me.lucyydotp.playout.controller.content.ScannedContent
import me.lucyydotp.playout.controller.output.OutputManager
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("AMCP server")

/** Starts the AMCP server. */
public suspend fun runAmcpServer(
    outputManager: OutputManager,
    scannedContent: ScannedContent,
    config: PlayoutConfig,
) {
    logger.info("Starting AMCP server on port ${config.amcp?.port}")
    val commandDispatcher = AmcpCommandDispatcher(outputManager, scannedContent, config)
    val selectorManager = SelectorManager()
    val server =
        aSocket(selectorManager).tcp().bind("0.0.0.0", config.amcp?.port?.toInt() ?: return)
    supervisorScope {
        while (isActive) {
            val sock = server.accept()
            logger.info("Accepted connection from ${sock.remoteAddress}")
            launch { handleSocket(sock, commandDispatcher) }
        }
    }
}

/** Handles incoming messages for a socket. */
private suspend fun handleSocket(socket: Socket, commandDispatcher: AmcpCommandDispatcher) {
    val read = socket.openReadChannel()
    val write = socket.openWriteChannel(autoFlush = true)

    try {
        while (true) {
            val line = read.readLine() ?: return
            logger.info("From ${socket.remoteAddress}: $line")
            val parsed =
                try {
                    AmcpCommandParser.parse(line)
                } catch (e: AmcpCommandParseException) {
                    logger.error("Failed to parse command $line", e)
                    write.writeStringUtf8("400 ERROR\r\n")
                    continue
                }

            val response = commandDispatcher.dispatch(parsed.command)
            val responseWithId =
                if (parsed.id != null) {
                    "RES ${parsed.id} $response"
                } else response

            logger.info(
                "To ${socket.remoteAddress}: ${responseWithId.replace("\r", "\\r").replace("\n", "\\n")}"
            )
            write.writeStringUtf8(responseWithId)
        }
    } finally {
        logger.info("Connection closed")
    }
}
