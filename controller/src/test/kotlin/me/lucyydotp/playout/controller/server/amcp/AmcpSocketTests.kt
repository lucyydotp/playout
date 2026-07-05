package me.lucyydotp.playout.controller.server.amcp

import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertEquals
import me.lucyydotp.playout.controller.output.Output
import me.lucyydotp.playout.controller.output.OutputManager
import org.junit.jupiter.api.Test

class AmcpSocketTests {
    private fun mockOutputs(): Pair<OutputManager, Output> {
        val output = mockk<Output>()

        val manager = mockk<OutputManager>()
        every { manager["1"] } returns output
        every { manager[any()] } throws NoSuchElementException()

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
}
