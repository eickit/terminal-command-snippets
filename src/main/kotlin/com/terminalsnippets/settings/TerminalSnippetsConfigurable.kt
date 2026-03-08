package com.terminalsnippets.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.ProjectManager
import javax.swing.JComponent

/**
 * Registers the settings panel under File → Settings → Tools → Terminal Command Snippets.
 */
class TerminalSnippetsConfigurable : Configurable {

    private var panel: TerminalSnippetsSettingsPanel? = null

    override fun getDisplayName(): String = "Terminal Command Snippets"

    override fun createComponent(): JComponent {
        // Get active project (for native file dialogs)
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
