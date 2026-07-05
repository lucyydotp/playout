package me.lucyydotp.playout.controller.output

import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.JsonObject
import me.lucyydotp.playout.config.OutputConfig
import me.lucyydotp.playout.content.ContentReference
import me.lucyydotp.playout.state.LayerState

private fun <K, V> Map<K, V>.replace(key: K, valueMapper: (V) -> V): Map<K, V> {
    var changed = false
    val newMap = mapValues { (k, v) ->
        if (k == key) {
            changed = true
            valueMapper(v)
        } else {
            v
        }
    }
    if (!changed) {
        throw NoSuchElementException(key.toString())
    }
    return newMap
}

/** A single output. */
public class Output(public val name: String, public val config: OutputConfig) {

    private val logger = org.slf4j.LoggerFactory.getLogger("Output: $name")

    public val state: StateFlow<Map<Int, LayerState>>
        field = MutableStateFlow(emptyMap())

    /**
     * Loads content onto a layer, immediately removing any content already on the layer.
     *
     * @param layer the layer to load content onto
     * @param content the content to load
     * @param templateData the template data to use for the content
     * @return the state of the layer after loading
     */
    public fun load(
        layer: Int,
        content: ContentReference,
        templateData: JsonObject = JsonObject(emptyMap()),
    ): LayerState {
        val layerState = LayerState(UUID.randomUUID(), false, content, templateData)
        state.update { state -> state + mapOf(layer to layerState) }
        logger.info("Loading $content onto layer $layer")
        return layerState
    }

    /**
     * Plays the content loaded on a layer.
     *
     * @throws NoSuchElementException if the layer has no loaded content to play
     */
    public fun play(layer: Int) {
        state.update { state -> state.replace(layer) { it.copy(isPlaying = true) } }
        logger.info("Playing content on layer $layer")
    }

    /**
     * Updates the template data for a layer.
     *
     * @param layer the layer to update
     * @param templateData the new template data
     * @throws NoSuchElementException if the layer has no loaded content to update
     */
    public fun update(layer: Int, templateData: JsonObject) {
        state.update { state -> state.replace(layer) { it.copy(templateData = templateData) } }
        logger.info("Updating template data for layer $layer: $templateData")
    }

    /**
     * Stops the content currently playing on a layer. The content remains loaded.
     *
     * @throws NoSuchElementException if the layer has no content currently playing
     */
    public fun stop(layer: Int) {
        state.update { state -> state.replace(layer) { it.copy(isPlaying = false) } }
        logger.info("Stopping content on layer $layer")
    }
}
