package me.lucyydotp.playout.state

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import me.lucyydotp.playout.content.ContentReference
import me.lucyydotp.playout.serialization.SerializableUUID
import me.lucyydotp.playout.util.EMPTY

/** The state of an output layer. */
@Serializable
public data class LayerState(
    /**
     * A unique ID for this instance of the graphic attached to the layer.
     *
     * This value will remain the same while a single graphic has its template data updated. If it
     * changes, the graphic is removed immediately and replaced with a new one.
     */
    public val id: SerializableUUID,

    /**
     * The content's current step.
     *
     * The first step is step 0 - graphics must support step 0 but may support any amount of
     * subsequent steps. The graphic is considered stopped if the step is negative, or if the step
     * is higher than the graphic supports.
     */
    public val currentStep: Int,

    /** The content being played. */
    public val content: ContentReference,

    /** The template data associated with the layer. */
    public val templateData: JsonObject = JsonObject.EMPTY,
)
