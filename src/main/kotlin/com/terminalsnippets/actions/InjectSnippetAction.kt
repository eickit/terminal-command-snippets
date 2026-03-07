package com.terminalsnippets.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.terminalsnippets.model.TerminalSnippet
import com.terminalsnippets.util.TerminalHelper

/**
 * Eine einzelne Snippet-Action im Menü.
 *
 * Darstellung:
 *  - Normal:      "Name  –  Beschreibung"
 *  - Highlighted: "<html><b>Name</b>  –  Beschreibung</html>"  (fett)
 *
 * Tooltip (description): der eigentliche Befehl
 */
class InjectSnippetAction(private val snippet: TerminalSnippet) : AnAction() {

    init {
        val displayText = buildDisplayText(snippet)
        templatePresentation.text = displayText
        templatePresentation.description = snippet.command
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        TerminalHelper.insertCommand(project, snippet.command)
    }

    companion object {
        fun buildDisplayText(snippet: TerminalSnippet): String {
            val hasDesc = snippet.description.isNotBlank()
            // Immer HTML, damit der Befehl hellgrau dargestellt werden kann
            return buildString {
                append("<html>")
                if (snippet.highlighted) {
                    append("<b>").append(escapeHtml(snippet.name)).append("</b>")
                } else {
                    append(escapeHtml(snippet.name))
                }
                if (hasDesc) {
                    append("  –  ").append(escapeHtml(snippet.description))
                }
                append("  <span style='color:gray;'>").append(escapeHtml(snippet.command)).append("</span>")
                append("</html>")
            }
        }

        private fun escapeHtml(text: String) =
            text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    }
}
