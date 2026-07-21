package me.lucyydotp.playout.content

import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.jupiter.api.assertThrows

class ContentReferenceTests {

    @Test
    fun `types without content throw`() {
        assertThrows<IllegalArgumentException> { ContentReference.fromString("foo") }
    }

    @Test
    fun `uppercase solid-color strings decode to the correct color`() {
        assertEquals(
            ContentReference.SolidColor(0x2468acu),
            ContentReference.fromString("solid-color:#2468AC"),
        )
    }

    @Test
    fun `lowercase solid-color strings decode to the correct color`() {
        assertEquals(
            ContentReference.SolidColor(0x2468acu),
            ContentReference.fromString("solid-color:#2468ac"),
        )
    }

    @Test
    fun `solid-color strings without a leading hash throw`() {
        assertThrows<IllegalArgumentException> { ContentReference.fromString("solid-color:123456") }
    }

    @Test
    fun `solid-color strings with missing digits throw`() {
        assertThrows<IllegalArgumentException> { ContentReference.fromString("solid-color:#12345") }
    }

    @Test
    fun `OGraf strings decode to the provided path`() {
        assertEquals(
            ContentReference.OGraf("foo/bar/baz"),
            ContentReference.fromString("ograf:foo/bar/baz"),
        )
    }
}
