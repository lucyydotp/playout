package me.lucyydotp.playout.util

import kotlinx.serialization.json.JsonObject

private val EMPTY_JSON_OBJECT = JsonObject(emptyMap())

/** An empty [JsonObject]. */
public val JsonObject.Companion.EMPTY: JsonObject
    get() = EMPTY_JSON_OBJECT
