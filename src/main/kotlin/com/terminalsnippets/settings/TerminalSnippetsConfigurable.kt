package com.terminalsnippets.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.ProjectManager
import javax.swing.JComponent

/**
 * Registriert das Settings-Panel unter File → Settings → Tools → Terminal Snippets.
 */
class TerminalSnippetsConfigurable : Configurable {

    private var panel: TerminalSnippetsSettingsPanel? = null

    override fun getDisplayName(): String = "Terminal Snippets"

    override fun createComponent(): JComponent {
        // Aktives Projekt holen (für native Datei-Dialoge)
        val project = ProjectManager.getInstance().openProjects.firstOrNull()
        val p = TerminalSnippetsSettingsPanel(project)
        panel = p
        p.loadFrom(TerminalSnippetsState.getInstance().categories)
        return p.mainPanel
    }

    override fun isModified(): Boolean =
        panel?.isModified(TerminalSnippetsState.getInstance().categories) ?: false

    override fun apply() {
        val state = TerminalSnippetsState.getInstance()
        val newCategories = panel?.getCategories() ?: return
        state.categories.clear()
        state.categories.addAll(newCategories)
    }

    override fun reset() {
        panel?.loadFrom(TerminalSnippetsState.getInstance().categories)
    }

    override fun disposeUIResources() {
        panel = null
    }
}
