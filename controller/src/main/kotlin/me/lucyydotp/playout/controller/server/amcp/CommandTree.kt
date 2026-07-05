package me.lucyydotp.playout.controller.server.amcp

/**
 * A tree of AMCP commands.
 */
public sealed interface CommandTree {
    /**
     * A branch that contains subcommands.
     */
    public data class Branch(public val children: Map<String, CommandTree>) : CommandTree

    /**
     * An executable subcommand.
     */
    public fun interface Command : CommandTree {
        /**
         * Runs the command.
         * @param args the arguments to the command. Does not include the command name.
         */
        public fun handle(args: List<String>): String
    }


    public class Builder {
        private sealed interface Buildable
        private data class MutableBranch(val children: MutableMap<String, Buildable> = mutableMapOf()) :
            Buildable

        private data class MutableCommand(val handler: Command) : Buildable

        private val root = MutableBranch()

        /**
         * Registers a new command.
         */
        public operator fun String.invoke(handler: Command) {
            val leafNode = split(" ").dropLast(1).fold(root) { node, arg ->
                node.children.getOrPut(arg, ::MutableBranch) as? MutableBranch
                    ?: throw IllegalStateException("Command node $arg already exists, cannot use as a branch")
            }

            if (leafNode.children.putIfAbsent(
                    substringAfterLast(" "),
                    MutableCommand(handler),
                ) != null) {
                throw IllegalStateException("Command $this already exists")
            }
        }


        private fun build(branch: MutableBranch): CommandTree.Branch = Branch(
            branch.children.mapValues { (_, v) ->
                when (v) {
                    is MutableCommand -> v.handler
                    is MutableBranch -> build(v)
                }
            },
        )

        /**
         * Builds an immutable [CommandTree] from this builder.
         */
        public fun build(): CommandTree.Branch = build(root)
    }
}

/**
 * Builds a [CommandTree] using the provided [builder].
 */
public inline fun CommandTree(builder: CommandTree.Builder.() -> Unit): CommandTree.Branch =
    CommandTree.Builder().apply(builder).build()

/**
 * Finds a command node for a command.
 * @return a pair of the command to execute, and the args to provide it, or null if no such command exists
 */
public fun CommandTree.Branch.find(splitCommand: List<String>): Pair<CommandTree.Command, List<String>>? {
    var i = 0
    var node: CommandTree? = this
    while (i < splitCommand.size) {
        node = (node as? CommandTree.Branch)?.children?.get(splitCommand[i++])
        if (node is CommandTree.Command) break
    }

    return ((node as? CommandTree.Command) ?: return null) to splitCommand.drop(i)
}
