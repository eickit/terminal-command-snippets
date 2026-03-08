package com.terminalsnippets.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.popup.JBPopupFactory
import java.awt.Component

class OpenSnippetsPopupAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        // Pass SnippetActionGroup directly – never call getChildren() from client code
        val group = SnippetActionGroup()

        val popup = JBPopupFactory.getInstance()
            .createActionGroupPopup(
                "Terminal Command Snippets",
                group,
                e.dataContext,
                JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                true
            )

        val component: Component? = e.inputEvent?.component
        if (component != null) {
            popup.showUnderneathOf(component)
        } else {
            popup.showInFocusCenter()
        }
    }
}
