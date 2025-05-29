package dev.nyon.autodrop.extensions

import com.mojang.brigadier.StringReader

/**
 * Constructs a [StringReader] from a string.
 */
fun String.stringReader(): StringReader {
    return StringReader(this)
}

/**
 * Add * wildcard as an item type if no type is specified to match item predicate syntax.
 */
fun String.matchItemPredicate(): String {
    return if (startsWith("[")) "*${this}" else this
}