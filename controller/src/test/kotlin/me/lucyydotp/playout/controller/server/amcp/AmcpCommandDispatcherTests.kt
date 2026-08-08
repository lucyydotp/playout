package me.lucyydotp.playout.controller.server.amcp

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import me.lucyydotp.playout.content.ContentReference
import me.lucyydotp.playout.controller.output.Output
import me.lucyydotp.playout.controller.output.OutputManager
import me.lucyydotp.playout.util.EMPTY
import org.junit.jupiter.api.Nested

class AmcpCommandDispatcherTests {
    private fun mockOutputs(realOutput: Boolean = false): Pair<OutputManager, Output> {
        val output = if (realOutput) Output("1", mockk()) else mockk<Output>()

        val manager = mockk<OutputManager>()
        every { manager.get(channel = any()) } throws NoSuchElementException()
        every { manager.byAcmpId } returns mapOf(1 to output)

        return manager to output
    }

    @Test
    fun `PING with no args`() {
        val (manager) = mockOutputs()
        val dispatcher = AmcpCommandDispatcher(manager, mockk(), mockk())

        assertEquals("PONG", dispatcher.dispatch(AmcpCommand.Ping(emptyList())))
    }

    @Test
    fun `PING with args`() {
        val (manager) = mockOutputs()
        val dispatcher = AmcpCommandDispatcher(manager, mockk(), mockk())

        assertEquals("PONG foo bar", dispatcher.dispatch(AmcpCommand.Ping(listOf("foo", "bar"))))
    }

    @Nested
    inner class CG {
        @Test
        fun `CG ADD loads content without playing when play-on-load is 0`() {
            val (manager, output) = mockOutputs(true)
            val dispatcher = AmcpCommandDispatcher(manager, mockk(), mockk())
            val response =
                dispatcher.dispatch(
                    AmcpCommand.CgAdd(
                        channel = 1,
                        layer = 10,
                        template = ContentReference.SolidColor(0xffffffu),
                        playOnLoad = false,
                        templateData = JsonObject.EMPTY,
                    )
                )
            assertEquals("202 CG OK\r\n", response)

            val state = output.state.value[10]
            assertNotNull(state)
            assertEquals(state.currentStep, -1)
            assertEquals(state.templateData, JsonObject(emptyMap()))
        }

        @Test
        fun `CG ADD loads content and plays when play-on-load is 1`() {
            val (manager, output) = mockOutputs(true)
            val dispatcher = AmcpCommandDispatcher(manager, mockk(), mockk())
            val response =
                dispatcher.dispatch(
                    AmcpCommand.CgAdd(
                        channel = 1,
                        layer = 10,
                        template = ContentReference.SolidColor(0xffffffu),
                        playOnLoad = true,
                        templateData = JsonObject.EMPTY,
                    )
                )
            assertEquals("202 CG OK\r\n", response)

            val state = output.state.value[10]
            assertNotNull(state)
            assertEquals(state.currentStep, 0)
            assertEquals(state.templateData, JsonObject(emptyMap()))
        }

        @Test
        fun `CG PLAY plays the layer's existing content`() {
            val (manager, output) = mockOutputs()
            every { output.play(any()) } returns Unit

            val dispatcher = AmcpCommandDispatcher(manager, mockk(), mockk())
            val response = dispatcher.dispatch(AmcpCommand.CgPlay(channel = 1, layer = 10))

            assertEquals("202 CG OK\r\n", response)
            verify { output.play(10) }
        }

        @Test
        fun `CG PLAY errors when playing on nonexistent layers`() {
            val (manager, output) = mockOutputs()
            every { output.play(any()) } throws NoSuchElementException()

            val dispatcher = AmcpCommandDispatcher(manager, mockk(), mockk())
            val response = dispatcher.dispatch(AmcpCommand.CgPlay(channel = 1, layer = 10))

            assertEquals("403 CG ERROR\r\n", response)
        }

        @Test
        fun `CG UPDATE updates the layer's content`() {
            val (manager, output) = mockOutputs()
            every { output.update(any(), any()) } returns Unit

            val dispatcher = AmcpCommandDispatcher(manager, mockk(), mockk())
            val json = JsonObject(mapOf("foo" to JsonPrimitive("bar")))
            val response = dispatcher.dispatch(AmcpCommand.CgUpdate(channel = 1, layer = 10, json))

            assertEquals("202 CG OK\r\n", response)
            verify { output.update(10, json) }
        }

        @Test
        fun `CG UPDATE errors when updating nonexistent layers`() {
            val (manager, output) = mockOutputs()
            every { output.update(any(), any()) } throws NoSuchElementException()
            val dispatcher = AmcpCommandDispatcher(manager, mockk(), mockk())
            val json = JsonObject(mapOf("foo" to JsonPrimitive("bar")))
            val response = dispatcher.dispatch(AmcpCommand.CgUpdate(channel = 1, layer = 10, json))

            assertEquals("403 CG ERROR\r\n", response)
        }
    }
}
