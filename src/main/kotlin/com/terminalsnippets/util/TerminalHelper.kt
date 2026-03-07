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
 * Fügt einen Befehl in das aktive Terminal ein.
 *
 * Strategie:
 *  1. Befehl in die AWT-System-Zwischenablage kopieren
 *  2. Terminal-ToolWindow fokussieren
 *  3. Paste-Action auslösen (mehrere Strategien)
 *  4. Notification als Bestätigung
 */
object TerminalHelper {

    fun insertCommand(project: Project, command: String) {
        if (command.isBlank()) return

        try {
            // 1. AWT System-Clipboard direkt setzen (nicht über CopyPasteManager)
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            clipboard.setContents(StringSelection(command), null)
        } catch (ex: Exception) {
            notify(project, "Fehler beim Kopieren: ${ex.message}", NotificationType.ERROR)
            return
        }

        // 2. Terminal aktivieren + fokussieren
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Terminal")
        if (toolWindow == null) {
            notify(project, "In Zwischenablage kopiert (kein Terminal gefunden). Einfügen mit ⌘V.", NotificationType.INFORMATION)
            return
        }

        toolWindow.activate({
            // 3. Paste im nächsten EDT-Zyklus (damit der Fokus auf dem Terminal liegt)
            ApplicationManager.getApplication().invokeLater({
                val pasted = tryPaste()
                if (!pasted) {
                    notify(project, "In Zwischenablage kopiert. Einfügen mit ⌘V / Ctrl+V.", NotificationType.INFORMATION)
                }
            }, ModalityState.nonModal())
        }, true)
    }

    /**
     * Versucht, Paste in der aktuell fokussierten Komponente auszulösen.
     * Probiert mehrere Strategien der Reihe nach.
     */
    private fun tryPaste(): Boolean {
        val focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().focusOwner
            ?: return false

        // Strategie 1: IntelliJ ACTION_PASTE
        try {
            val pasteAction = ActionManager.getInstance().getAction(IdeActions.ACTION_PASTE)
            if (pasteAction != null) {
                val result = ActionManager.getInstance().tryToExecute(
                    pasteAction, null, focusOwner, null, true
                )
                if (result.isDone) return true
            }
        } catch (_: Exception) { }

        // Strategie 2: $Paste Action (Editor-spezifisch)
        try {
            val editorPaste = ActionManager.getInstance().getAction("\$Paste")
            if (editorPaste != null) {
                val result = ActionManager.getInstance().tryToExecute(
                    editorPaste, null, focusOwner, null, true
                )
                if (result.isDone) return true
            }
        } catch (_: Exception) { }

        // Strategie 3: Direkt TransferHandler.importData aufrufen (Swing-Paste)
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
            // Notification-Gruppe nicht verfügbar – ignorieren
        }
    }
}
