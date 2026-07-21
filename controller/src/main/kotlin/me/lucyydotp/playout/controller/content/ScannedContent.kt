package me.lucyydotp.playout.controller.content

import me.lucyydotp.playout.content.ograf.OGrafGraphic

/** A set of content that a [ContentScanner] has scanned. */
public data class ScannedContent(
    /** OGraf graphics, mapped by ID. */
    public val ograf: Map<String, OGrafGraphic>
)
