package me.lucyydotp.playout.controller.output

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertSame
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import me.lucyydotp.playout.config.OutputConfig
import me.lucyydotp.playout.content.ContentReference
import me.lucyydotp.playout.model.Resolution
import org.junit.jupiter.api.assertThrows

private fun newOutput() = Output("test", OutputConfig(resolution = Resolution(1920u, 1080u)))

class OutputTests {
    // -- Load --
    @Test
    fun `loading adds missing layers`() {
        val output = newOutput()
        assertEquals(output.state.value, emptyMap())

        val content = ContentReference.SolidColor(0u)
        val templateData =
            JsonObject(mapOf("key1" to JsonPrimitive("value1"), "key2" to JsonPrimitive("value2")))

        output.load(1, content, templateData)

        val state = output.state.value

        assertEquals(1, state.size)
        assertEquals(content, state[1]?.content)
        assertEquals(templateData, state[1]?.templateData)
    }

    @Test
    fun `loading replaces existing layers`() {
        val output = newOutput()
        val firstState = output.load(1, ContentReference.SolidColor(0u))
        val secondState = output.load(1, ContentReference.SolidColor(0u))
        assertNotEquals(firstState.id, secondState.id)
    }

    // -- Play --
    @Test
    fun `playing sets playing to true`() {
        val output = newOutput()
        output.load(1, ContentReference.SolidColor(0u))
        assertEquals(false, output.state.value[1]?.isPlaying)

        output.play(1)
        assertEquals(true, output.state.value[1]?.isPlaying)
    }

    @Test
    fun `playing an already playing layer does nothing`() {
        val output = newOutput()
        output.load(1, ContentReference.SolidColor(0u))

        output.play(1)
        val firstState = output.state.value

        output.play(1)
        val secondState = output.state.value
        assertSame(firstState, secondState)
    }

    @Test
    fun `playing a layer with no content throws`() {
        val output = newOutput()
        assertThrows<NoSuchElementException> { output.play(1) }
    }

    // -- Update --
    @Test
    fun `updating template data replaces data without changing id`() {
        val output = newOutput()
        val firstState = output.load(1, ContentReference.SolidColor(0u))

        val data = JsonObject(mapOf("key" to JsonPrimitive("value")))
        output.update(1, data)
        val secondState = output.state.value[1]!!

        assertEquals(firstState.id, secondState.id)
        assertEquals(data, secondState.templateData)
    }

    // -- Stop --
    @Test
    fun `stopping sets playing to false`() {
        val output = newOutput()
        output.load(1, ContentReference.SolidColor(0u))
        assertEquals(false, output.state.value[1]?.isPlaying)

        output.play(1)
        assertEquals(true, output.state.value[1]?.isPlaying)

        output.stop(1)
        assertEquals(false, output.state.value[1]?.isPlaying)
    }

    @Test
    fun `stopping an already stopped layer does nothing`() {
        val output = newOutput()
        val state = output.load(1, ContentReference.SolidColor(0u))
        assertEquals(state.isPlaying, false)

        output.stop(1)
        assertSame(state, output.state.value[1])
    }
}
