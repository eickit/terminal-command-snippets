package com.terminalsnippets.util

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import java.awt.KeyboardFocusManager
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

/**
 * Inserts a command into the active terminal.
 *
 * Strategy:
 *  1. Copy command to the AWT system clipboard
 *  2. Focus the terminal tool window
 *  3. Trigger paste action (multiple strategies)
 *  4. Show notification as confirmation
 */
object TerminalHelper {

    fun insertCommand(project: Project, command: String) {
        if (command.isBlank()) return

        try {
            // 1. AWT System-Clipboard direkt setzen (nicht über CopyPasteManager)
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            clipboard.setContents(StringSelection(command), null)
        } catch (ex: Exception) {
            notify(project, "Error copying to clipboard: ${ex.message}", NotificationType.ERROR)
            return
        }

        // 2. Activate and focus the terminal
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Terminal")
        if (toolWindow == null) {
            notify(project, "Copied to clipboard (no terminal found). Paste with ⌘V.", NotificationType.INFORMATION)
            return
        }

        toolWindow.activate({
            // 3. Paste on the next EDT cycle (so the terminal has focus)
            ApplicationManager.getApplication().invokeLater({
                val pasted = tryPaste()
                if (!pasted) {
                    notify(project, "Copied to clipboard. Paste with ⌘V / Ctrl+V.", NotificationType.INFORMATION)
                }
            }, ModalityState.nonModal())
        }, true)
    }

    /**
     * Attempts to trigger paste in the currently focused component.
     * Tries multiple strategies in sequence.
     */
    private fun tryPaste(): Boolean {
        val focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().focusOwner
            ?: return false

        // Strategy 1: IntelliJ ACTION_PASTE
        try {
            val pasteAction = ActionManager.getInstance().getAction(IdeActions.ACTION_PASTE)
            if (pasteAction != null) {
                val result = ActionManager.getInstance().tryToExecute(
                    pasteAction, null, focusOwner, null, true
                )
                if (result.isDone) return true
            }
        } catch (_: Exception) { }

        // Strategy 2: $Paste action (editor-specific)
        try {
            val editorPaste = ActionManager.getInstance().getAction("\$Paste")
            if (editorPaste != null) {
                val result = ActionManager.getInstance().tryToExecute(
                    editorPaste, null, focusOwner, null, true
                )
                if (result.isDone) return true
            }
        } catch (_: Exception) { }

        // Strategy 3: Call TransferHandler.importData directly (Swing paste)
        try {
            if (focusOwner is javax.swing.JComponent) {
                val transferHandler = focusOwner.transferHandler
                if (transferHandler != null) {
                    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                    val data = clipboard.getContents(null)
                    if (data != null) {
                        transferHandler.importData(focusOwner, data)
                        return true
                    }
                }
            }
        } catch (_: Exception) { }

        return false
    }

    private fun notify(project: Project, message: String, type: NotificationType) {
        try {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("Terminal Snippets")
                .createNotification(message, type)
                .notify(project)
        } catch (_: Exception) {
            // Notification group not available – ignore
        }
    }
}
