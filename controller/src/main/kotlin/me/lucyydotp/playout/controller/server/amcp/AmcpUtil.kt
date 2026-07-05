package me.lucyydotp.playout.controller.server.amcp

/** Thrown when an AMCP command cannot be parsed. */
public class AmcpCommandParseException(
    message: String,
    public val command: String,
    cause: Throwable? = null,
) : Exception(message, cause)

/** Splits an AMCP command into its constituent parts. */
internal fun splitCommand(command: String) = buildList {
    var current = StringBuilder()
    var backslash = false
    var quoted = false
    var wasQuoted = false

    fun finishLine() {
        backslash = false
        quoted = false
        if (current.isNotBlank()) {
            add(current.toString())
            current = StringBuilder()
        }
    }

    for (c in command) {
        when {
            backslash -> {
                current.append(c)
                backslash = false
            }

            c == '\\' -> {
                backslash = true
            }

            c.isWhitespace() && !quoted -> {
                finishLine()
            }

            c == '"' -> {
                if (!quoted) {
                    if (current.isNotEmpty())
                        throw AmcpCommandParseException("Unescaped quote in argument", command)
                    wasQuoted = true
                }

                quoted = !quoted
            }

            else -> {
                if (wasQuoted && !quoted)
                    throw AmcpCommandParseException("Unescaped quote in argument", command)
                current.append(c)
            }
        }
    }
    if (quoted) throw AmcpCommandParseException("Unterminated quoted phrase", command)
    finishLine()
}

private val channelRegex = Regex("""^(\d+)(?:-(\d+))?$""")
private const val DEFAULT_LAYER = 9999

/**
 * Parses the channel and layer from an AMCP command.
 *
 * @throws AmcpCommandParseException if the channel command is invalid
 */
internal fun parseChannelAndLayer(string: String): Pair<Int, Int> {
    val matcher =
        channelRegex.matchEntire(string)
            ?: throw AmcpCommandParseException("Invalid channel", string)

    return try {
        matcher.groupValues[1].toInt() to
            (matcher.groupValues.getOrNull(2)?.takeIf { it.isNotBlank() }?.toInt() ?: DEFAULT_LAYER)
    } catch (e: NumberFormatException) {
        throw AmcpCommandParseException("Invalid channel", string, e)
    }
}
