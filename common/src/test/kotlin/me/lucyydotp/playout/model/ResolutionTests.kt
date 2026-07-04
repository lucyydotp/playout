package me.lucyydotp.playout.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.assertThrows

class ResolutionTests {
    private fun encode(value: Resolution) =
        Json.encodeToString(Resolution.Serializer, value).trim('"')

    private fun decode(value: String) = Json.decodeFromString(Resolution.Serializer, "\"$value\"")

    @Test
    fun `deserializes positive resolutions correctly`() {
        assertEquals(decode("1920x1080"), Resolution(width = 1920u, height = 1080u))

        assertEquals(decode("1280x720"), Resolution(width = 1280u, height = 720u))

        assertEquals(decode("1x1"), Resolution(width = 1u, height = 1u))
    }

    @Test
    fun `throws on zero width`() {
        assertThrows<SerializationException> { decode("0x1080") }
    }

    @Test
    fun `throws on zero height`() {
        assertThrows<SerializationException> { decode("1920x0") }
    }

    @Test
    fun `throws on negative width`() {
        assertThrows<SerializationException> { decode("-1920x1080") }
    }

    @Test
    fun `throws on negative height`() {
        assertThrows<SerializationException> { decode("1920x-1080") }
    }

    @Test
    fun `throws on invalid string`() {
        assertThrows<SerializationException> { decode("not a resolution") }
        assertThrows<SerializationException> { decode("1920x1080x1920") }
    }

    @Test
    fun `encodes correctly`() {
        assertEquals(encode(Resolution(1920u, 1080u)), "1920x1080")
        assertEquals(encode(Resolution(1280u, 720u)), "1280x720")
        assertEquals(encode(Resolution(1u, 1u)), "1x1")
    }
}
