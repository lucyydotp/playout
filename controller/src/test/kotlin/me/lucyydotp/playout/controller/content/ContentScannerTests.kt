package me.lucyydotp.playout.controller.content

import java.nio.file.Path
import kotlin.io.path.div
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.test.runTest
import me.lucyydotp.playout.config.ContentConfig
import me.lucyydotp.playout.config.PlayoutConfig

class ContentScannerTests {

    private fun config(testCase: String) =
        PlayoutConfig(
            outputs = emptyMap(),
            content = ContentConfig(Path.of("src/test/resources/content-scanner-test", testCase)),
        )

    @Test
    fun `content is loaded correctly`() = runTest {
        val config = config("correct")
        val scanned = ContentScanner(config).scan()
        assertEquals(2, scanned.ograf.size)

        val lowerThird = scanned.ograf["dev.ograf.tutorial.lower-third"]
        assertNotNull(lowerThird)
        assertEquals("CBS-Style Lower Third", lowerThird.manifest.name)
        assertEquals(Path.of("lower-third/lower-third.ograf.json"), lowerThird.manifestPath)
        assertEquals(config.content.path / "ograf" / "lower-third.zip", lowerThird.archivePath)

        val newsTicker = scanned.ograf["dev.ograf.tutorial.ticker"]
        assertNotNull(newsTicker)
        assertEquals("News Ticker", newsTicker.manifest.name)
        assertEquals(Path.of("ticker/ticker.ograf.json"), newsTicker.manifestPath)
        assertEquals(config.content.path / "ograf" / "ticker.zip", newsTicker.archivePath)
    }

    @Test
    fun `malformed manifests are ignored`() = runTest {
        val scanned = ContentScanner(config("malformed")).scan()
        assert(scanned.ograf.isEmpty())
    }
}
