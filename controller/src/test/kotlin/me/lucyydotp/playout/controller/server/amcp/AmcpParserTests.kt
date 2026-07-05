package me.lucyydotp.playout.controller.server.amcp

import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows

class AmcpParserTests {
    @Test
    fun `unquoted words are split individually`() {
        assertEquals(listOf("foo", "bar", "baz"), splitCommand("foo bar baz"))
    }

    @Test
    fun `escaped spaces are ignored`() {
        assertEquals(listOf("foo bar", "baz"), splitCommand("foo\\ bar baz"))
    }

    @Test
    fun `quoted phrases are treated as a single part`() {
        assertEquals(listOf("foo", "bar baz"), splitCommand("foo \"bar baz\""))
    }

    @Test
    fun `escaped quotes are ignored`() {
        assertEquals(listOf("foo", "\"bar", "baz\""), splitCommand("""foo \"bar baz\""""))
    }

    @Test
    fun `missing end quote throws`() {
        assertThrows<AmcpCommandParseException> { splitCommand("""foo "bar""") }
    }

    @Test
    fun `multiple quotes without spaces throw`() {
        assertThrows<AmcpCommandParseException> { splitCommand("""foo "bar""baz""") }
    }

    @Test
    fun `trailing quote text throws`() {
        assertThrows<AmcpCommandParseException> { splitCommand("""foo "bar"baz""") }
    }

    @Test
    fun `quote in string throws`() {
        assertThrows<AmcpCommandParseException> { splitCommand("""foo bar"baz""") }
    }

    @Test
    fun `escaped end quote throws`() {
        assertThrows<AmcpCommandParseException> { splitCommand("""foo "bar\"""") }
    }

    @Nested
    inner class ChannelAndLayerParser {
        @Test
        fun `channel without layer is parsed properly`() {
            assertEquals(1 to 9999, parseChannelAndLayer("1"))
        }

        @Test
        fun `channel with layer is parsed properly`() {
            assertEquals(1 to 50, parseChannelAndLayer("1-50"))
        }

        @Test
        fun `invalid channel throws`() {
            assertThrows<AmcpCommandParseException> { parseChannelAndLayer("foo") }
        }

        @Test
        fun `channel with negative channel throws`() {
            assertThrows<AmcpCommandParseException> { parseChannelAndLayer("-1-10") }
        }

        @Test
        fun `channel with negative layer throws`() {
            assertThrows<AmcpCommandParseException> { parseChannelAndLayer("0--10") }
        }
    }
}
