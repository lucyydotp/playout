package me.lucyydotp.playout.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A reference to a known piece of content. */
@Serializable
public sealed interface ContentReference {

    /**
     * A solid colour.
     *
     * @param color The ARGB colour to display.
     */
    @Serializable
    @SerialName("color")
    public data class SolidColor(val color: UInt) : ContentReference

    /**
     * An OGraf graphic.
     *
     * TODO: implement this
     */
    @Serializable @SerialName("ograf") public data class OGraf(val id: String) : ContentReference
}
