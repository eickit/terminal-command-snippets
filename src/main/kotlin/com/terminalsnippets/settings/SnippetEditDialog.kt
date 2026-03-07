package com.terminalsnippets.settings

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.util.ui.JBUI
import com.terminalsnippets.model.TerminalSnippet
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

/**
 * Dialog zum Erstellen und Bearbeiten eines Terminal-Snippets.
 */
class SnippetEditDialog(
    private val snippet: TerminalSnippet,
    private val isNew: Boolean
) : DialogWrapper(true) {

    private val nameField        = JTextField(snippet.name, 35)
    private val commandField     = JTextField(snippet.command, 35)
    private val descriptionField = JTextField(snippet.description, 35)
    private val highlightedBox   = JCheckBox("Im Menü fett hervorheben", snippet.highlighted)

    init {
        title = if (isNew) "Snippet hinzufügen" else "Snippet bearbeiten"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(GridBagLayout())
        panel.preferredSize = Dimension(450, 165)
        panel.border = JBUI.Borders.empty(8)

        val lc = GridBagConstraints().apply {
            gridx = 0
            anchor = GridBagConstraints.WEST
            insets = JBUI.insets(4, 0, 4, 8)
        }
        val fc = GridBagConstraints().apply {
            gridx = 1
            fill = GridBagConstraints.HORIZONTAL
            weightx = 1.0
            insets = JBUI.insets(4, 0, 4, 0)
        }

        // Name
        lc.gridy = 0; fc.gridy = 0
        panel.add(JLabel("Name:"), lc)
        panel.add(nameField, fc)

        // Befehl
        lc.gridy = 1; fc.gridy = 1
        panel.add(JLabel("Befehl:"), lc)
        panel.add(commandField, fc)

        // Beschreibung (optional)
        lc.gridy = 2; fc.gridy = 2
        panel.add(JLabel("Beschreibung:"), lc)
        panel.add(descriptionField, fc)

        // Hervorhebung – Checkbox überspannt beide Spalten
        val checkConstraints = GridBagConstraints().apply {
            gridx = 0
            gridy = 3
            gridwidth = 2
            anchor = GridBagConstraints.WEST
            insets = JBUI.insets(6, 0, 2, 0)
        }
        panel.add(highlightedBox, checkConstraints)

        return panel
    }

    override fun doValidate(): ValidationInfo? {
        if (nameField.text.isBlank())    return ValidationInfo("Name darf nicht leer sein.", nameField)
        if (commandField.text.isBlank()) return ValidationInfo("Befehl darf nicht leer sein.", commandField)
        return null
    }

    /** Übernimmt die Eingaben in das übergebene Snippet-Objekt. */
    fun applyToSnippet(): TerminalSnippet {
        snippet.name        = nameField.text.trim()
        snippet.command     = commandField.text.trim()
        snippet.description = descriptionField.text.trim()
        snippet.highlighted = highlightedBox.isSelected
        return snippet
    }

    override fun getPreferredFocusedComponent(): JComponent = nameField
}
