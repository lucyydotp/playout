package me.lucyydotp.playout.controller.server.amcp

import io.ktor.util.CaseInsensitiveMap
import kotlinx.serialization.json.JsonObject
import me.lucyydotp.playout.content.ContentReference

/** A parsed AMCP command. See https://casparcg.com/docs/wiki/protocols/amcp-protocol. */
public sealed interface AmcpCommand {

    /** Replies with PONG. */
    public data class Ping(val args: List<String>) : AmcpCommand

    /** Lists available templates. */
    public data class Tls(val subDirectory: String?) : AmcpCommand

    /** Lists available media. This is a stub as media playback is not supported. */
    public object Cls : AmcpCommand

    /** Gets information about available channels. */
    public data class Info(val type: InfoType) : AmcpCommand {
        public enum class InfoType {
            Channels,
            Config;

            public companion object :
                Map<String, InfoType> by entries.associateByTo(CaseInsensitiveMap(), InfoType::name)
        }
    }

    /** Gets the server's version. */
    public object Version : AmcpCommand

    /** Loads a template into a layer. */
    public data class CgAdd(
        val channel: Int,
        val layer: Int,
        val template: ContentReference,
        val playOnLoad: Boolean,
        val templateData: JsonObject,
    ) : AmcpCommand

    /** Plays the content on a layer. */
    public data class CgPlay(val channel: Int, val layer: Int) : AmcpCommand

    /** Updates the template data for an existing graphic on a layer. */
    public data class CgUpdate(val channel: Int, val layer: Int, val templateData: JsonObject) :
        AmcpCommand

    /** Stops the content on a layer. */
    public data class CgStop(val channel: Int, val layer: Int) : AmcpCommand
}

/** A parsed AMCP command. */
public data class ParsedAmcpCommand(
    /** The contents of the command. */
    val command: AmcpCommand,
    /** The command's ID if specified. */
    val id: String?,
)
