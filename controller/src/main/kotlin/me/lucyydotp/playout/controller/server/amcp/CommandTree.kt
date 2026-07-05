package me.lucyydotp.playout.controller.server.amcp

import java.util.TreeMap

/**
 * The context for a specific execution of a command.
 */
public data class CommandContext(
    /**
     * The command to execute.
     */
    val command: CommandTree.Command,
    /**
     * The values of any wildcard arguments.
     */
    val wildcardValues: List<String>,

    /**
     * Arguments provided after the command.
     */
    val arguments: List<String>,
) {
    /**
     * Runs the command.
     */
    public operator fun invoke(): String = command.handle(this)
}

/** A tree of AMCP commands. */
public sealed interface CommandTree {
    public companion object {
        /**
         * The wildcard string used to match any command value.
         */
        public const val WILDCARD: String = "*"
    }

    /** A branch that contains subcommands. */
    public data class Branch(public val children: Map<String, CommandTree>) : CommandTree

    /** An executable subcommand. */
    public fun interface Command : CommandTree {
        /**
         * Runs the command.
         *
         * @param context the command context
         */
        public fun handle(context: CommandContext): String
    }

    public class Builder {
        private sealed interface Buildable

        private data class MutableBranch(
            val children: MutableMap<String, Buildable> = TreeMap(String.CASE_INSENSITIVE_ORDER)
        ) : Buildable

        private data class MutableCommand(val handler: Command) : Buildable

        private val root = MutableBranch()

        /** Registers a new command. */
        public operator fun String.invoke(handler: Command) {
            val leafNode =
                split(" ").dropLast(1).fold(root) { node, arg ->
                    node.children.getOrPut(arg, ::MutableBranch) as? MutableBranch
                        ?: throw IllegalStateException(
                            "Command node $arg already exists, cannot use as a branch"
                        )
                }

            if (
                leafNode.children.putIfAbsent(substringAfterLast(" "), MutableCommand(handler)) !=
                    null
            ) {
                throw IllegalStateException("Command $this already exists")
            }
        }

        private fun build(branch: MutableBranch): Branch =
            Branch(
                branch.children.mapValues { (_, v) ->
                    when (v) {
                        is MutableCommand -> v.handler
                        is MutableBranch -> build(v)
                    }
                }
            )

        /** Builds an immutable [CommandTree] from this builder. */
        public fun build(): Branch = build(root)
    }
}

/** Builds a [CommandTree] using the provided [builder]. */
public inline fun CommandTree(builder: CommandTree.Builder.() -> Unit): CommandTree.Branch =
    CommandTree.Builder().apply(builder).build()

/**
 * Finds a command node for a command.
 *
 * @return a pair of the command to execute, and the args to provide it, or null if no such command
 *   exists
 */
public fun CommandTree.Branch.find(
    splitCommand: List<String>
): CommandContext? {
    var i = 0
    var node: CommandTree? = this
    val wildcardValues = mutableListOf<String>()
    while (i < splitCommand.size) {
        val part = splitCommand[i++]
        node = (node as? CommandTree.Branch)?.children?.let {
            it[part] ?: (it[CommandTree.WILDCARD].also { wildcardValues += part })
        }
        if (node is CommandTree.Command) break
    }

    return CommandContext(
        node as? CommandTree.Command ?: return null,
        wildcardValues,
        splitCommand.drop(i)
    )
}
