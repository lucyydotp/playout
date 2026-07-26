package me.lucyydotp.playout.controller.server.amcp

import kotlin.test.assertEquals
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import me.lucyydotp.playout.content.ContentReference
import me.lucyydotp.playout.util.EMPTY
import org.junit.jupiter.api.Test

class AmcpCommandParserTests {
    @Test
    fun `PING with no args`() {
        val response = AmcpCommandParser.parse("PING")
        assertEquals(ParsedAmcpCommand(AmcpCommand.Ping(emptyList()), null), response)
    }

    @Test
    fun `PING with args`() {
        val response = AmcpCommandParser.parse("PING one two three")
        assertEquals(
            ParsedAmcpCommand(AmcpCommand.Ping(listOf("one", "two", "three")), null),
            response,
        )
    }

    @Test
    fun `PING with args and REQ`() {
        val response = AmcpCommandParser.parse("REQ message-id PING one two three")
        assertEquals(
            ParsedAmcpCommand(AmcpCommand.Ping(listOf("one", "two", "three")), "message-id"),
            response,
        )
    }

    @Test
    fun `CG ADD, play-on-load 0`() {
        assertEquals(
            ParsedAmcpCommand(
                AmcpCommand.CgAdd(
                    channel = 1,
                    layer = 10,
                    template = ContentReference.SolidColor(0xffffffu),
                    playOnLoad = false,
                    templateData = JsonObject.EMPTY,
                ),
                null,
            ),
            AmcpCommandParser.parse("CG 1-10 ADD 0 solid-color:#ffffff 0"),
        )
    }

    @Test
    fun `CG ADD, play-on-load 1`() {
        assertEquals(
            ParsedAmcpCommand(
                AmcpCommand.CgAdd(
                    channel = 1,
                    layer = 10,
                    template = ContentReference.SolidColor(0xffffffu),
                    playOnLoad = true,
                    templateData = JsonObject.EMPTY,
                ),
                null,
            ),
            AmcpCommandParser.parse("CG 1-10 ADD 0 solid-color:#ffffff 1"),
        )
    }

    @Test
    fun `CG ADD with template data`() {
        assertEquals(
            ParsedAmcpCommand(
                AmcpCommand.CgAdd(
                    channel = 1,
                    layer = 10,
                    template = ContentReference.SolidColor(0xffffffu),
                    playOnLoad = false,
                    templateData = JsonObject(mapOf("foo" to JsonPrimitive("bar"))),
                ),
                null,
            ),
            AmcpCommandParser.parse("""CG 1-10 ADD 0 solid-color:#ffffff 0 "{\"foo\": \"bar\"}""""),
        )
    }

    @Test
    fun `CG PLAY`() {
        assertEquals(
            ParsedAmcpCommand(AmcpCommand.CgPlay(1, 10), null),
            AmcpCommandParser.parse("CG 1-10 PLAY"),
        )
    }

    @Test
    fun `CG UPDATE`() {
        assertEquals(
            ParsedAmcpCommand(
                AmcpCommand.CgUpdate(
                    1,
                    10,
                    templateData = JsonObject(mapOf("foo" to JsonPrimitive("bar"))),
                ),
                null,
            ),
            AmcpCommandParser.parse("""CG 1-10 UPDATE 0 "{\"foo\": \"bar\"}""""),
        )
    }

    @Test
    fun `CG STOP`() {
        assertEquals(
            ParsedAmcpCommand(AmcpCommand.CgStop(1, 10), null),
            AmcpCommandParser.parse("CG 1-10 STOP"),
        )
    }
}
