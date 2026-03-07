package com.terminalsnippets.model

/**
 * Ein einzelner Terminal-Befehl (Snippet).
 * Benötigt No-Arg-Konstruktor für IntelliJ-XML-Serialisierung.
 */
class TerminalSnippet {
    var name: String = ""
    var command: String = ""
    var description: String = ""
    /** Wird im Menü fett dargestellt wenn true. */
    var highlighted: Boolean = false

    constructor()

    constructor(name: String, command: String, description: String = "", highlighted: Boolean = false) {
        this.name = name
        this.command = command
        this.description = description
        this.highlighted = highlighted
    }

    fun deepCopy(): TerminalSnippet = TerminalSnippet(name, command, description, highlighted)

    override fun toString(): String = name
}

/**
 * Eine Kategorie, die mehrere Snippets gruppiert.
 * Benötigt No-Arg-Konstruktor für IntelliJ-XML-Serialisierung.
 */
class SnippetCategory {
    var name: String = ""
    var snippets: MutableList<TerminalSnippet> = mutableListOf()

    constructor()

    constructor(name: String) {
        this.name = name
    }

    fun deepCopy(): SnippetCategory {
        val copy = SnippetCategory(name)
        copy.snippets = snippets.map { it.deepCopy() }.toMutableList()
        return copy
    }

    override fun toString(): String = name
}
