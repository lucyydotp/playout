package me.lucyydotp.playout.controller.server.amcp

import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows

class CommandTreeTests {

    @Nested
    inner class Builder {
        @Test
        fun `simple top-level commands are inserted into the tree`() {
            val one = CommandTree.Command { "one" }
            val two = CommandTree.Command { "two" }

            val tree = CommandTree {
                "one"(one)
                "two"(two)
            }

            assertEquals(CommandTree.Branch(mapOf("one" to one, "two" to two)), tree)
        }

        @Test
        fun `nested commands are inserted into the tree`() {
            val one = CommandTree.Command { "one" }
            val two = CommandTree.Command { "two" }
            val three = CommandTree.Command { "three" }

            val tree = CommandTree {
                "foo one"(one)
                "foo two"(two)
                "bar"(three)
            }

            assertEquals(
                CommandTree.Branch(
                    mapOf(
                        "foo" to CommandTree.Branch(mapOf("one" to one, "two" to two)),
                        "bar" to three,
                    )
                ),
                tree,
            )
        }

        @Test
        fun `duplicate commands throw`() {
            assertThrows<IllegalStateException> {
                CommandTree {
                    "foo" { "bar" }
                    "foo" { "baz" }
                }
            }
        }

        @Test
        fun `replacing a command with a branch throws`() {
            assertThrows<IllegalStateException> {
                CommandTree {
                    "foo" { "bar" }
                    "foo bar" { "baz" }
                }
            }
        }

        @Test
        fun `replacing a branch with a command throws`() {
            assertThrows<IllegalStateException> {
                CommandTree {
                    "foo bar" { "baz" }
                    "foo" { "bar" }
                }
            }
        }
    }

    @Nested
    inner class `Find nodes` {
        @Test
        fun `finds existing shallow nodes with no arguments`() {
            val handler = CommandTree.Command { "foo" }
            val tree = CommandTree { "foo"(handler) }

            assertEquals(
                CommandContext(handler, emptyList(), emptyList()),
                tree.find(listOf("foo")),
            )
        }

        @Test
        fun `finds existing shallow nodes with arguments`() {
            val handler = CommandTree.Command { "foo" }
            val tree = CommandTree { "foo"(handler) }

            assertEquals(
                CommandContext(handler, emptyList(), listOf("bar", "baz")),
                tree.find(listOf("foo", "bar", "baz")),
            )
        }

        @Test
        fun `finds existing deep nodes with arguments`() {
            val handler = CommandTree.Command { "foo" }
            val tree = CommandTree { "foo bar"(handler) }

            assertEquals(
                CommandContext(handler, emptyList(), listOf("baz")),
                tree.find(listOf("foo", "bar", "baz")),
            )
        }

        @Test
        fun `returns null for non-existent nodes`() {
            val tree = CommandTree { "foo" { "foo" } }
            assertEquals(null, tree.find(listOf("bar")))
        }

        @Test
        fun `returns null for partial deep commands`() {
            val tree = CommandTree { "foo bar baz" { "qux" } }
            assertEquals(null, tree.find(listOf("foo", "bar")))
        }

        @Test
        fun `wildcard nodes match any value`() {
            val handler = CommandTree.Command { "foo" }
            val tree = CommandTree { "foo *"(handler) }

            assertEquals(
                CommandContext(handler, listOf("bar"), emptyList()),
                tree.find(listOf("foo", "bar")),
            )
        }

        @Test
        fun `wildcard nodes match any value with other commands afterwards`() {
            val handler = CommandTree.Command { "foo" }
            val tree = CommandTree { "foo * baz"(handler) }

            assertEquals(
                CommandContext(handler, listOf("bar"), emptyList()),
                tree.find(listOf("foo", "bar", "baz")),
            )
        }

        @Test
        fun `wildcard nodes accept arguments afterwards`() {
            val handler = CommandTree.Command { "foo" }
            val tree = CommandTree { "foo *"(handler) }

            assertEquals(
                CommandContext(handler, listOf("bar"), listOf("baz")),
                tree.find(listOf("foo", "bar", "baz")),
            )
        }

        @Test
        fun `literal nodes take precedence over wildcards`() {
            val one = CommandTree.Command { "one" }
            val two = CommandTree.Command { "two" }

            val tree = CommandTree {
                "foo bar"(one)
                "foo *"(two)
            }

            assertEquals(
                CommandContext(one, emptyList(), emptyList()),
                tree.find(listOf("foo", "bar")),
            )
            assertEquals(
                CommandContext(two, listOf("baz"), emptyList()),
                tree.find(listOf("foo", "baz")),
            )
        }
    }
}
