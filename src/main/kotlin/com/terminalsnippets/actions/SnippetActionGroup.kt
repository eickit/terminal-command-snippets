package com.terminalsnippets.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.Separator
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAware
import com.terminalsnippets.settings.TerminalSnippetsConfigurable
import com.terminalsnippets.settings.TerminalSnippetsState

/**
 * Main ActionGroup for the terminal toolbar.
 *
 * Attached as a dropdown button (popup="true") to TerminalToolwindowActionGroup.
 * getChildren() builds the menu dynamically from the current state:
 *
 *   ▸ Symfony          ← DefaultActionGroup (popup=true)
 *       Cache Clear
 *       Make Controller
 *       …
 *   ▸ Composer
 *       …
 *   ─────────────────
 *   ⚙ Manage Snippets…
 */
class SnippetActionGroup : ActionGroup(), DumbAware {

    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        val state = TerminalSnippetsState.getInstance()
        val filledCategories = state.categories.filter { it.snippets.isNotEmpty() }

        if (filledCategories.isEmpty()) {
            return arrayOf(NoSnippetsAction())
        }

        val actions = mutableListOf<AnAction>()

        for (category in filledCategories) {
            // Jede Kategorie = echtes Untermenü (popup=true)
            val catGroup = DefaultActionGroup(category.name, true)
            catGroup.templatePresentation.icon = AllIcons.Nodes.Folder

            for (snippet in category.snippets) {
                catGroup.add(InjectSnippetAction(snippet))
            }

            actions.add(catGroup)
        }

        actions.add(Separator.getInstance())
        actions.add(OpenSnippetsManagerAction())

        return actions.toTypedArray()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = true
    }

    // ─── Inner helper actions ─────────────────────────────────────────────────

    /** Shown when no snippets have been configured yet. */
    private class NoSnippetsAction :
        AnAction("No snippets configured", "Add snippets in the settings", null) {
        override fun actionPerformed(e: AnActionEvent) {
            val project = e.project ?: return
            ShowSettingsUtil.getInstance()
                .showSettingsDialog(project, TerminalSnippetsConfigurable::class.java)
        }
        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = false
        }
    }

    /** Last entry in the menu – opens the settings. */
    private class OpenSnippetsManagerAction :
        AnAction("Manage Snippets…", "Open Terminal Command Snippets settings", AllIcons.General.Settings) {
        override fun actionPerformed(e: AnActionEvent) {
            val project = e.project ?: return
            ShowSettingsUtil.getInstance()
                .showSettingsDialog(project, TerminalSnippetsConfigurable::class.java)
        }
    }
}
