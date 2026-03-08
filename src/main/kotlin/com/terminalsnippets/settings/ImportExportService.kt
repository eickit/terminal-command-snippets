package com.terminalsnippets.settings

import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.terminalsnippets.model.SnippetCategory
import com.terminalsnippets.model.TerminalSnippet
import java.awt.Component
import java.awt.FileDialog
import java.awt.Frame
import javax.swing.SwingUtilities
import javax.swing.JOptionPane

/**
 * Handles JSON-based import and export.
 *
 * Export: uses java.awt.FileDialog (native OS save dialog, no deprecated IntelliJ API)
 * Import: uses IntelliJ's FileChooser (VirtualFile required for reading)
 *
 * JSON format:
 * {
 *   "version": 1,
 *   "categories": [
 *     {
 *       "name": "Symfony",
 *       "snippets": [
 *         { "name": "Cache Clear", "command": "php bin/console cache:clear", "description": "..." }
 *       ]
 *     }
 *   ]
 * }
 */
object ImportExportService {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    // ─── JSON data classes ────────────────────────────────────────────────────

    private data class JsonSnippet(
        val name: String,
        val command: String,
        val description: String = "",
        val highlighted: Boolean = false
    )

    private data class JsonCategory(
        val name: String,
        val snippets: List<JsonSnippet> = emptyList()
    )

    private data class JsonBackup(
        val version: Int = 1,
        val categories: List<JsonCategory> = emptyList()
    )

    // ─── Export ──────────────────────────────────────────────────────────────

    /**
     * Opens the native OS save dialog (java.awt.FileDialog) and exports all categories as JSON.
     * Avoids deprecated IntelliJ FileSaverDescriptor API entirely.
     */
    fun exportToFile(parent: Component, categories: List<SnippetCategory>) {
        val parentFrame = SwingUtilities.getWindowAncestor(parent) as? Frame

        val fileDialog = FileDialog(parentFrame, "Export Snippets", FileDialog.SAVE).apply {
            file = "terminal-snippets-backup.json"
            isVisible = true
        }

        val dir      = fileDialog.directory ?: return
        val filename = fileDialog.file       ?: return

        val outputFile = java.io.File(dir, if (filename.endsWith(".json", ignoreCase = true)) filename else "$filename.json")

        try {
            val backup = JsonBackup(
                categories = categories.map { cat ->
                    JsonCategory(
                        name = cat.name,
                        snippets = cat.snippets.map { s ->
                            JsonSnippet(s.name, s.command, s.description, s.highlighted)
                        }
                    )
                }
            )

            outputFile.writeText(gson.toJson(backup), Charsets.UTF_8)

            JOptionPane.showMessageDialog(
                parent,
                "${categories.size} category/categories exported to:\n${outputFile.name}",
                "Export Successful",
                JOptionPane.INFORMATION_MESSAGE
            )
        } catch (ex: Exception) {
            JOptionPane.showMessageDialog(
                parent,
                "Error during export:\n${ex.message}",
                "Export Failed",
                JOptionPane.ERROR_MESSAGE
            )
        }
    }

    // ─── Import ──────────────────────────────────────────────────────────────

    /**
     * Opens the native file dialog, reads the JSON file and returns
     * the resulting categories. Asks for the import mode beforehand.
     */
    fun importFromFile(
        parent: Component,
        existingCategories: List<SnippetCategory>,
        project: Project? = null
    ): List<SnippetCategory>? {

        val descriptor = FileChooserDescriptor(
            /* chooseFiles = */ true,
            /* chooseFolders = */ false,
            /* chooseJars = */ false,
            /* chooseJarsAsFiles = */ false,
            /* chooseJarContents = */ false,
            /* chooseMultiple = */ false
        ).apply {
            title = "Import Snippets"
            description = "Select a JSON backup file"
            withFileFilter { it.extension?.equals("json", ignoreCase = true) == true }
        }

        val virtualFile = FileChooser.chooseFile(descriptor, project, null) ?: return null

        // Parse JSON
        val imported = try {
            val json = String(virtualFile.contentsToByteArray(), Charsets.UTF_8)
            parseJson(json)
        } catch (ex: JsonSyntaxException) {
            JOptionPane.showMessageDialog(
                parent,
                "Invalid JSON format:\n${ex.message}",
                "Import Failed",
                JOptionPane.ERROR_MESSAGE
            )
            return null
        } catch (ex: Exception) {
            JOptionPane.showMessageDialog(
                parent,
                "Error reading file:\n${ex.message}",
                "Import Failed",
                JOptionPane.ERROR_MESSAGE
            )
            return null
        }

        if (imported.isEmpty()) {
            JOptionPane.showMessageDialog(
                parent, "The file contains no categories.", "Import", JOptionPane.INFORMATION_MESSAGE
            )
            return null
        }

        // Ask for import mode
        val mode = askImportMode(parent, imported.size, existingCategories.size) ?: return null

        val result = when (mode) {
            ImportMode.REPLACE -> imported
            ImportMode.MERGE   -> mergeCategories(existingCategories, imported)
        }

        JOptionPane.showMessageDialog(
            parent,
            "${imported.size} category/categories imported.",
            "Import Successful",
            JOptionPane.INFORMATION_MESSAGE
        )

        return result
    }

    // ─── Helper functions ─────────────────────────────────────────────────────

    private fun parseJson(json: String): List<SnippetCategory> {
        val backup = gson.fromJson(json, JsonBackup::class.java)
        return backup.categories.map { cat ->
            SnippetCategory(cat.name).also { category ->
                category.snippets.addAll(
                    cat.snippets.map { s -> TerminalSnippet(s.name, s.command, s.description, s.highlighted) }
                )
            }
        }
    }

    /**
     * Merge strategy:
     * - Same category name → add missing snippets (no duplicate by command)
     * - New category → append at the end
     */
    private fun mergeCategories(
        existing: List<SnippetCategory>,
        imported: List<SnippetCategory>
    ): List<SnippetCategory> {
        val result = existing.map { it.deepCopy() }.toMutableList()

        for (importedCat in imported) {
            val existingCat = result.find { it.name.equals(importedCat.name, ignoreCase = true) }
            if (existingCat != null) {
                val existingCommands = existingCat.snippets.map { it.command.trim() }.toSet()
                importedCat.snippets
                    .filter { it.command.trim() !in existingCommands }
                    .forEach { existingCat.snippets.add(it.deepCopy()) }
            } else {
                result.add(importedCat.deepCopy())
            }
        }
        return result
    }

    private enum class ImportMode { MERGE, REPLACE }

    private fun askImportMode(parent: Component, importCount: Int, existingCount: Int): ImportMode? {
        val options = arrayOf("Merge", "Replace", "Cancel")
        val result = JOptionPane.showOptionDialog(
            parent,
            "The file contains $importCount category/categories.\n" +
            "Currently $existingCount category/categories exist.\n\n" +
            "How would you like to import?",
            "Choose Import Mode",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        )
        return when (result) {
            0    -> ImportMode.MERGE
            1    -> ImportMode.REPLACE
            else -> null
        }
    }
}
