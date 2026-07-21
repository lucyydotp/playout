package me.lucyydotp.playout.content.ograf

import java.nio.file.Path

/** A loaded OGraf graphic. */
public data class OGrafGraphic(
    /** The graphic's manifest. */
    val manifest: OGrafManifest,

    /** The path to the graphic's archive on disk. */
    val archivePath: Path,

    /** The path to the graphic's manifest file within the archive. */
    val manifestPath: Path,
)
