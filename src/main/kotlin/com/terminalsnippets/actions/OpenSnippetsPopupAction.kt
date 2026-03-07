package com.terminalsnippets.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.util.ui.JBUI
import java.awt.Component

class OpenSnippetsPopupAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val snippetGroup = SnippetActionGroup()
        val children = snippetGroup.getChildren(e)

        val group = DefaultActionGroup(*children)

        val popup = JBPopupFactory.getInstance()
            .createActionGroupPopup(
                "Terminal Snippets",
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
