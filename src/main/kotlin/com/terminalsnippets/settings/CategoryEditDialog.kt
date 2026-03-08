package com.terminalsnippets.settings

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.util.ui.JBUI
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

/**
 * Dialog for creating and renaming a category.
 */
class CategoryEditDialog(private val initialName: String = "") : DialogWrapper(true) {

    private val nameField = JTextField(initialName, 30)

    init {
        title = if (initialName.isEmpty()) "Add Category" else "Rename Category"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(GridBagLayout())
        panel.preferredSize = Dimension(380, 60)
        panel.border = JBUI.Borders.empty(8)

        val labelConstraints = GridBagConstraints().apply {
            gridx = 0
            gridy = 0
            anchor = GridBagConstraints.WEST
            insets = JBUI.insets(4, 0, 4, 8)
        }
        val fieldConstraints = GridBagConstraints().apply {
            gridx = 1
            gridy = 0
            fill = GridBagConstraints.HORIZONTAL
            weightx = 1.0
            insets = JBUI.insets(4, 0, 4, 0)
        }

        panel.add(JLabel("Name:"), labelConstraints)
        panel.add(nameField, fieldConstraints)

        return panel
    }

    override fun doValidate(): ValidationInfo? {
        if (nameField.text.isBlank()) return ValidationInfo("Name must not be empty.", nameField)
        return null
    }

    fun getCategoryName(): String = nameField.text.trim()

    override fun getPreferredFocusedComponent(): JComponent = nameField
}
