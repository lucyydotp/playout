package me.lucyydotp.playout.controller.server.amcp

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import me.lucyydotp.playout.controller.output.Output
import me.lucyydotp.playout.controller.output.OutputManager
import org.junit.jupiter.api.Nested

class AmcpSocketTests {
    private fun mockOutputs(realOutput: Boolean = false): Pair<OutputManager, Output> {
        val output = if (realOutput) Output("1", mockk()) else mockk<Output>()

        val manager = mockk<OutputManager>()
        every { manager.get(channel = any()) } throws NoSuchElementException()
        every { manager.get(channel = "1") } returns output

        return manager to output
    }

    @Test
    fun `PING with no args`() {
        val (manager) = mockOutputs()
        val socket = AmcpSocket(manager)

        assertEquals("PONG", socket.handle("PING"))
    }

    @Test
    fun `PING with args`() {
        val (manager) = mockOutputs()
        val socket = AmcpSocket(manager)

        assertEquals("PONG foo bar", socket.handle("PING foo bar"))
    }

    @Nested
    inner class CG {
        @Test
        fun `CG ADD loads content without playing when play-on-load is 0`() {
            val (manager, output) = mockOutputs(true)

            val response = AmcpSocket(manager).handle("CG 1-10 ADD 0 template-name 0")
            assertEquals("202 OK\r\n", response)

            val state = output.state.value[10]
            assertNotNull(state)
            assertEquals(state.isPlaying, false)
            assertEquals(state.templateData, JsonObject(emptyMap()))
        }

        @Test
        fun `CG ADD loads content and plays when play-on-load is 1`() {
            val (manager, output) = mockOutputs(true)

            val response = AmcpSocket(manager).handle("CG 1-10 ADD 0 template-name 1")
            assertEquals("202 OK\r\n", response)

            val state = output.state.value[10]
            assertNotNull(state)
            assertEquals(state.isPlaying, true)
            assertEquals(state.templateData, JsonObject(emptyMap()))
        }

        @Test
        fun `CG ADD parses JSON data`() {
            val (manager, output) = mockOutputs(true)

            val response = AmcpSocket(manager).handle("""CG 1-10 ADD 0 template-name 0 "{\"foo\": \"bar\"}"""")
            assertEquals("202 OK\r\n", response)

            val state = output.state.value[10]
            assertNotNull(state)
            assertEquals(state.templateData, JsonObject(mapOf("foo" to JsonPrimitive("bar"))))
        }

        @Test
        fun `CG PLAY plays the layer's existing content`() {
            val (manager, output) = mockOutputs()
            every { output.play(any()) } returns Unit

            val response = AmcpSocket(manager).handle("CG 1-10 PLAY 0")
            assertEquals("202 OK\r\n", response)
            verify { output.play(10) }
        }

        @Test
        fun `CG PLAY errors when playing on nonexistent layers`() {
            val (manager, output) = mockOutputs()
            every { output.play(any()) } throws NoSuchElementException()

            val response = AmcpSocket(manager).handle("CG 1-10 PLAY 0")
            assertEquals("403 CG ERROR\r\n", response)
        }
    }
}
