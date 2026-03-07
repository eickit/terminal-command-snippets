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
 * Haupt-ActionGroup für die Terminal-Toolbar.
 *
 * Wird als Dropdown-Button (popup="true") in TerminalToolwindowActionGroup eingehängt.
 * getChildren() baut das Menü dynamisch aus dem aktuellen State auf:
 *
 *   ▸ Symfony          ← DefaultActionGroup (popup=true)
 *       Cache Clear
 *       Make Controller
 *       …
 *   ▸ Composer
 *       …
 *   ─────────────────
 *   ⚙ Snippets verwalten…
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

    // ─── Innere Hilfs-Actions ─────────────────────────────────────────────────

    /** Wird angezeigt, wenn noch keine Snippets vorhanden sind. */
    private class NoSnippetsAction :
        AnAction("Keine Snippets konfiguriert", "Snippets in den Einstellungen anlegen", null) {
        override fun actionPerformed(e: AnActionEvent) {
            val project = e.project ?: return
            ShowSettingsUtil.getInstance()
                .showSettingsDialog(project, TerminalSnippetsConfigurable::class.java)
        }
        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = false
        }
    }

    /** Letzter Eintrag im Menü – öffnet die Einstellungen. */
    private class OpenSnippetsManagerAction :
        AnAction("Snippets verwalten…", "Terminal Snippets Einstellungen öffnen", AllIcons.General.Settings) {
        override fun actionPerformed(e: AnActionEvent) {
            val project = e.project ?: return
            ShowSettingsUtil.getInstance()
                .showSettingsDialog(project, TerminalSnippetsConfigurable::class.java)
        }
    }
}
