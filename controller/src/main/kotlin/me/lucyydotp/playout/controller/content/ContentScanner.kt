package me.lucyydotp.playout.controller.content

import java.util.zip.ZipFile
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.relativeTo
import kotlin.io.path.walk
import kotlinx.serialization.json.Json
import me.lucyydotp.playout.config.PlayoutConfig
import me.lucyydotp.playout.content.ograf.OGrafGraphic
import me.lucyydotp.playout.content.ograf.OGrafManifest
import org.slf4j.LoggerFactory

/** Scans a directory for content and creates a list of known content references. */
public class ContentScanner(private val config: PlayoutConfig) {

    private companion object {
        /** The folder within the content path containing OGraf archives. */
        private const val OGRAF_FOLDER = "ograf"

        /** The JSON decoder for OGraf manifests. */
        private val ografJson = Json { ignoreUnknownKeys = true }
    }

    private val logger = LoggerFactory.getLogger(ContentScanner::class.java)

    private fun scanOGraf(): Map<String, OGrafGraphic> {
        val ografFolder = config.content.path / OGRAF_FOLDER
        return ografFolder
            .walk()
            .filter { it.extension == "zip" }
            .flatMap { archivePath ->
                val relativePath = archivePath.relativeTo(ografFolder)
                logger.info("Scanning OGraf archive: $relativePath")
                val zip = ZipFile(archivePath.toFile())

                val manifests =
                    zip.entries().asSequence().filter { it.name.endsWith(".ograf.json") }.toList()

                if (manifests.isEmpty()) {
                    logger.warn("No OGraf manifests found in $archivePath")
                    return@flatMap emptyList()
                }

                manifests.mapNotNull { manifestPath ->
                    val manifest =
                        try {
                            ografJson.decodeFromString<OGrafManifest>(
                                zip.getInputStream(manifestPath).reader().readText()
                            )
                        } catch (ex: Exception) {
                            logger.error(
                                "Failed to parse OGraf manifest ${manifestPath.name} in $relativePath",
                                ex,
                            )
                            null
                        } ?: return@mapNotNull null

                    val graphic = OGrafGraphic(manifest, archivePath, Path(manifestPath.name))
                    logger.info("Found graphic ${manifest.id} (${manifest.name})")
                    graphic
                }
            }
            .groupingBy { it.manifest.id }
            .reduce { key, _, el ->
                logger.warn("Duplicate OGraf graphic with ID $key")
                el
            }
    }

    /** Scans the content path for content. */
    public fun scan(): ScannedContent {
        if (!config.content.path.exists()) {
            throw IllegalStateException("Content path ${config.content.path} does not exist")
        }
        return ScannedContent(ograf = scanOGraf())
    }
}
