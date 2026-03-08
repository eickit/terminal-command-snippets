package com.terminalsnippets.settings

import com.intellij.icons.AllIcons
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import com.terminalsnippets.model.SnippetCategory
import com.terminalsnippets.model.TerminalSnippet
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import javax.swing.*
import javax.swing.table.AbstractTableModel

/**
 * Haupt-Settings-Panel mit zwei Bereichen:
 *  - Links: Kategorie-Liste mit Toolbar (Add / Rename / Delete)
 *  - Rechts: Snippets-Tabelle für die gewählte Kategorie (Add / Edit / Delete)
 */
class TerminalSnippetsSettingsPanel(private val project: com.intellij.openapi.project.Project? = null) {

    val mainPanel: JPanel = JPanel(BorderLayout(10, 0))

    private val categoryListModel = DefaultListModel<SnippetCategory>()
    private val categoryList = JBList(categoryListModel)

    private val snippetTableModel = SnippetTableModel()
    private val snippetTable = JBTable(snippetTableModel)

    private var currentCategory: SnippetCategory? = null

    init {
        setupUI()
    }

    // ─── UI-Aufbau ──────────────────────────────────────────────────────────

    private fun setupUI() {
        mainPanel.border = JBUI.Borders.empty(8)

        // ── Kategorie-Liste (links) ──────────────────────────────────────────
        categoryList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        categoryList.cellRenderer = CategoryCellRenderer()

        // AnAction statt AnActionButton verwenden (addExtraAction(AnActionButton) ist deprecated)
        val renameAction = object : com.intellij.openapi.actionSystem.AnAction(
            "Rename", "Rename category", AllIcons.Actions.Edit
        ) {
            override fun actionPerformed(e: com.intellij.openapi.actionSystem.AnActionEvent) = renameCategory()
        }

        val categoryDecorator = ToolbarDecorator.createDecorator(categoryList)
            .setAddAction { addCategory() }
            .setRemoveAction { removeCategory() }
            .addExtraAction(renameAction)
            .createPanel()

        categoryDecorator.preferredSize = Dimension(200, 0)

        val leftPanel = JPanel(BorderLayout(0, 4))
        leftPanel.add(JLabel("Categories"), BorderLayout.NORTH)
        leftPanel.add(categoryDecorator, BorderLayout.CENTER)

        // ── Snippet-Tabelle (rechts) ─────────────────────────────────────────
        snippetTable.setShowGrid(true)
        snippetTable.selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION
        snippetTable.columnModel.getColumn(0).apply {
            preferredWidth = 30
            maxWidth = 30
            cellRenderer = StarColumnRenderer()
        }
        snippetTable.columnModel.getColumn(1).preferredWidth = 150  // Name
        snippetTable.columnModel.getColumn(2).preferredWidth = 240  // Befehl
        snippetTable.columnModel.getColumn(3).preferredWidth = 180  // Beschreibung

        val snippetDecorator = ToolbarDecorator.createDecorator(snippetTable)
            .setAddAction { addSnippet() }
            .setEditAction { editSnippet() }
            .setRemoveAction { removeSnippet() }
            .createPanel()

        val rightPanel = JPanel(BorderLayout(0, 4))
        rightPanel.add(JLabel("Snippets"), BorderLayout.NORTH)  // "Snippets" is the same in English
        rightPanel.add(snippetDecorator, BorderLayout.CENTER)

        mainPanel.add(leftPanel, BorderLayout.WEST)
        mainPanel.add(rightPanel, BorderLayout.CENTER)

        // ── Import / Export (unten) ──────────────────────────────────────────
        val importBtn = JButton("Import…").apply {
            icon = AllIcons.Actions.Upload
            addActionListener { doImport() }
        }
        val exportBtn = JButton("Export…").apply {
            icon = AllIcons.Actions.Download
            addActionListener { doExport() }
        }

        val ioPanel = JPanel().apply {
            layout = javax.swing.BoxLayout(this, javax.swing.BoxLayout.X_AXIS)
            border = JBUI.Borders.emptyTop(8)
            add(importBtn)
            add(javax.swing.Box.createHorizontalStrut(8))
            add(exportBtn)
            add(javax.swing.Box.createHorizontalGlue())
        }
        mainPanel.add(ioPanel, BorderLayout.SOUTH)

        // ── Kategorieauswahl aktualisiert die Snippet-Tabelle ─────────────────
        categoryList.addListSelectionListener { e ->
            if (!e.valueIsAdjusting) {
                val idx = categoryList.selectedIndex
                currentCategory = if (idx >= 0) categoryListModel.getElementAt(idx) else null
                snippetTableModel.setCategory(currentCategory)
            }
        }
    }

    // ─── Kategorien-Aktionen ─────────────────────────────────────────────────

    private fun addCategory() {
        val dialog = CategoryEditDialog()
        if (dialog.showAndGet()) {
            val cat = SnippetCategory(dialog.getCategoryName())
            categoryListModel.addElement(cat)
            categoryList.selectedIndex = categoryListModel.size - 1
        }
    }

    private fun removeCategory() {
        val idx = categoryList.selectedIndex
        if (idx < 0) return
        val name = categoryListModel.getElementAt(idx).name
        val confirm = JOptionPane.showConfirmDialog(
            mainPanel,
            "Delete category \"$name\" and all its snippets?",
            "Delete Category",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        )
        if (confirm == JOptionPane.YES_OPTION) {
            categoryListModel.remove(idx)
            if (categoryListModel.size > 0)
                categoryList.selectedIndex = minOf(idx, categoryListModel.size - 1)
        }
    }

    private fun renameCategory() {
        val idx = categoryList.selectedIndex
        if (idx < 0) return
        val cat = categoryListModel.getElementAt(idx)
        val dialog = CategoryEditDialog(cat.name)
        if (dialog.showAndGet()) {
            cat.name = dialog.getCategoryName()
            categoryListModel.set(idx, cat) // Neuzeichnen erzwingen
        }
    }

    // ─── Snippet-Aktionen ────────────────────────────────────────────────────

    private fun addSnippet() {
        val cat = currentCategory ?: run {
            JOptionPane.showMessageDialog(
                mainPanel,
                "Please select a category first.",
                "No Category Selected",
                JOptionPane.INFORMATION_MESSAGE
            )
            return
        }
        val newSnippet = TerminalSnippet()
        val dialog = SnippetEditDialog(newSnippet, isNew = true)
        if (dialog.showAndGet()) {
            dialog.applyToSnippet()
            cat.snippets.add(newSnippet)
            snippetTableModel.fireTableDataChanged()
            snippetTable.selectionModel.setSelectionInterval(
                cat.snippets.size - 1, cat.snippets.size - 1
            )
        }
    }

    private fun editSnippet() {
        val cat = currentCategory ?: return
        val row = snippetTable.selectedRow
        if (row < 0 || row >= cat.snippets.size) return
        val snippet = cat.snippets[row]
        val dialog = SnippetEditDialog(snippet, isNew = false)
        if (dialog.showAndGet()) {
            dialog.applyToSnippet()
            snippetTableModel.fireTableRowsUpdated(row, row)
        }
    }

    private fun removeSnippet() {
        val cat = currentCategory ?: return
        val row = snippetTable.selectedRow
        if (row < 0 || row >= cat.snippets.size) return
        cat.snippets.removeAt(row)
        snippetTableModel.fireTableDataChanged()
    }

    // ─── Daten laden / speichern ─────────────────────────────────────────────

    /** Lädt Kategorien aus dem State in das Panel (deep copy, um Änderungen rückgängig zu machen). */
    fun loadFrom(categories: List<SnippetCategory>) {
        categoryListModel.clear()
        categories.forEach { categoryListModel.addElement(it.deepCopy()) }
        if (categoryListModel.size > 0) categoryList.selectedIndex = 0
    }

    /** Gibt alle aktuell im Panel enthaltenen Kategorien zurück. */
    fun getCategories(): List<SnippetCategory> =
        (0 until categoryListModel.size).map { categoryListModel.getElementAt(it) }

    /** Prüft, ob sich die Daten gegenüber dem übergebenen Originalzustand geändert haben. */
    fun isModified(original: List<SnippetCategory>): Boolean {
        val current = getCategories()
        if (current.size != original.size) return true
        for (i in current.indices) {
            val c = current[i]; val o = original[i]
            if (c.name != o.name || c.snippets.size != o.snippets.size) return true
            for (j in c.snippets.indices) {
                val cs = c.snippets[j]; val os = o.snippets[j]
                if (cs.name != os.name || cs.command != os.command ||
                    cs.description != os.description || cs.highlighted != os.highlighted)
                    return true
            }
        }
        return false
    }

    // ─── Import / Export ─────────────────────────────────────────────────────

    private fun doExport() {
        ImportExportService.exportToFile(mainPanel, getCategories(), project)
    }

    private fun doImport() {
        val result = ImportExportService.importFromFile(mainPanel, getCategories(), project) ?: return
        loadFrom(result)
    }

    // ─── Hilfsklassen ─────────────────────────────────────────────────────────

    /** Table-Model für die Snippets der aktuell gewählten Kategorie. */
    inner class SnippetTableModel : AbstractTableModel() {
        // Column 0 = "★" (highlight), 1 = Name, 2 = Command, 3 = Description
        private val columns = arrayOf("★", "Name", "Command", "Description")
        private var category: SnippetCategory? = null

        fun setCategory(cat: SnippetCategory?) {
            category = cat
            fireTableDataChanged()
        }

        override fun getRowCount() = category?.snippets?.size ?: 0
        override fun getColumnCount() = columns.size
        override fun getColumnName(col: Int) = columns[col]
        override fun isCellEditable(row: Int, col: Int) = false
        override fun getColumnClass(col: Int) = String::class.java

        override fun getValueAt(row: Int, col: Int): Any {
            val s = category?.snippets?.getOrNull(row) ?: return ""
            return when (col) {
                0    -> s.highlighted
                1    -> s.name
                2    -> s.command
                3    -> s.description
                else -> ""
            }
        }
    }

    /** Renderer for the ★ column: shows "★" when highlighted, otherwise empty. */
    private inner class StarColumnRenderer : javax.swing.table.DefaultTableCellRenderer() {
        init {
            horizontalAlignment = SwingConstants.CENTER
        }

        override fun getTableCellRendererComponent(
            table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int
        ): Component {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
            val highlighted = value as? Boolean ?: false
            text = if (highlighted) "★" else ""
            toolTipText = if (highlighted) "Highlighted in menu (bold)" else null
            return this
        }
    }

    /** Renderer for the category list: shows icon + name + snippet count. */
    private inner class CategoryCellRenderer : DefaultListCellRenderer() {
        override fun getListCellRendererComponent(
            list: JList<*>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean
        ): Component {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
            if (value is SnippetCategory) {
                text = "${value.name}  (${value.snippets.size})"
                icon = AllIcons.Nodes.Folder
            }
            return this
        }
    }
}
