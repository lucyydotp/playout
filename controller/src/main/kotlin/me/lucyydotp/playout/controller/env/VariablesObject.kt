package me.lucyydotp.playout.controller.env

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

/** Parses and provides environment variables. */
public abstract class VariablesObject(public val prefix: String) {

    /** An eagerly-parsed environment variable. */
    @PublishedApi
    internal inner class EnvironmentVariable<T>(
        val name: String,
        val type: KType,
        val default: T?,
    ) : ReadOnlyProperty<Any?, T> {

        /**
         * Parses a string value to a value of type [T].
         *
         * @throws IllegalArgumentException if the string is invalidly formatted
         */
        private fun parseValue(value: String): T {
            if (type.isMarkedNullable && value.isBlank() || value == "null") return null as T
            return when (type.classifier) {
                Boolean::class -> value.toBoolean()
                Int::class -> value.toInt()
                Long::class -> value.toLong()
                Float::class -> value.toFloat()
                Double::class -> value.toDouble()
                UShort::class -> value.toUShort()
                else -> Json.decodeFromString(serializer(type), value)
            }
                as T
        }

        private val value = run {
            val fullEnvName = "${prefix}_$name"
            // If there's a value specified, return it, even if it's null.
            System.getenv(fullEnvName)?.let {
                try {
                    return@run parseValue(it)
                } catch (e: IllegalArgumentException) {
                    throw IllegalArgumentException("Failed to parse environment variable $name", e)
                }
            }
            // Return the default value if present.
            default?.let {
                return@run it
            }

            if (type.isMarkedNullable) {
                @Suppress("CAST_NEVER_SUCCEEDS")
                return@run null as T
            } else {
                throw IllegalStateException("Missing required environment variable $fullEnvName")
            }
        }

        override operator fun getValue(thisRef: Any?, property: KProperty<*>): T = value
    }

    /** Creates a new environment variable with a default value. */
    public inline infix fun <reified T> String.default(default: T): ReadOnlyProperty<Any?, T> =
        EnvironmentVariable(this, typeOf<T>(), default)

    /** Creates a new environment variable with no default. */
    public inline operator fun <reified T> String.provideDelegate(
        thisRef: Any?,
        prop: Any?,
    ): ReadOnlyProperty<Any?, T> = EnvironmentVariable(this, typeOf<T>(), null)
}
