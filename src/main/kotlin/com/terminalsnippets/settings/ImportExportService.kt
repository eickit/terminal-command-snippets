package com.terminalsnippets.settings

import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.terminalsnippets.model.SnippetCategory
import com.terminalsnippets.model.TerminalSnippet
import java.awt.Component
import javax.swing.JOptionPane

/**
 * Handles JSON-based import and export using IntelliJ's native file dialogs.
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

    // ─── Datenklassen für JSON ────────────────────────────────────────────────

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
     * Öffnet den nativen Speichern-Dialog und exportiert alle Kategorien als JSON.
     */
    fun exportToFile(parent: Component, categories: List<SnippetCategory>, project: Project? = null) {
        val descriptor = FileSaverDescriptor(
            "Snippets exportieren",
            "Alle Kategorien und Snippets als JSON speichern",
            "json"
        )

        val dialog = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, project)
        // Expliziter Cast auf VirtualFile? löst die Ambiguität mit Path?
        val fileWrapper = dialog.save(null as VirtualFile?, "terminal-snippets-backup") ?: return

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

            // Dateiendung sicherstellen (IntelliJ hängt sie manchmal nicht automatisch an)
            val rawFile = fileWrapper.file
            val outputFile = if (rawFile.name.endsWith(".json", ignoreCase = true)) {
                rawFile
            } else {
                java.io.File("${rawFile.absolutePath}.json")
            }

            outputFile.writeText(gson.toJson(backup), Charsets.UTF_8)

            JOptionPane.showMessageDialog(
                parent,
                "${categories.size} Kategorie(n) exportiert nach:\n${outputFile.name}",
                "Export erfolgreich",
                JOptionPane.INFORMATION_MESSAGE
            )
        } catch (ex: Exception) {
            JOptionPane.showMessageDialog(
                parent,
                "Fehler beim Exportieren:\n${ex.message}",
                "Export fehlgeschlagen",
                JOptionPane.ERROR_MESSAGE
            )
        }
    }

    // ─── Import ──────────────────────────────────────────────────────────────

    /**
     * Öffnet den nativen Datei-Dialog, liest die JSON-Datei und gibt die
     * resultierenden Kategorien zurück. Fragt vorher nach dem Import-Modus.
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
            title = "Snippets importieren"
            description = "JSON-Backup-Datei auswählen"
            withFileFilter { it.extension?.equals("json", ignoreCase = true) == true }
        }

        val virtualFile = FileChooser.chooseFile(descriptor, project, null) ?: return null

        // JSON parsen
        val imported = try {
            val json = String(virtualFile.contentsToByteArray(), Charsets.UTF_8)
            parseJson(json)
        } catch (ex: JsonSyntaxException) {
            JOptionPane.showMessageDialog(
                parent,
                "Ungültiges JSON-Format:\n${ex.message}",
                "Import fehlgeschlagen",
                JOptionPane.ERROR_MESSAGE
            )
            return null
        } catch (ex: Exception) {
            JOptionPane.showMessageDialog(
                parent,
                "Fehler beim Lesen der Datei:\n${ex.message}",
                "Import fehlgeschlagen",
                JOptionPane.ERROR_MESSAGE
            )
            return null
        }

        if (imported.isEmpty()) {
            JOptionPane.showMessageDialog(
                parent, "Die Datei enthält keine Kategorien.", "Import", JOptionPane.INFORMATION_MESSAGE
            )
            return null
        }

        // Import-Modus abfragen
        val mode = askImportMode(parent, imported.size, existingCategories.size) ?: return null

        val result = when (mode) {
            ImportMode.REPLACE -> imported
            ImportMode.MERGE   -> mergeCategories(existingCategories, imported)
        }

        JOptionPane.showMessageDialog(
            parent,
            "${imported.size} Kategorie(n) importiert.",
            "Import erfolgreich",
            JOptionPane.INFORMATION_MESSAGE
        )

        return result
    }

    // ─── Hilfsfunktionen ─────────────────────────────────────────────────────

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
     * Merge-Strategie:
     * - Gleicher Kategoriename → fehlende Snippets ergänzen (kein Duplikat per Befehl)
     * - Neue Kategorie → ans Ende anfügen
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
        val options = arrayOf("Zusammenführen (Merge)", "Ersetzen (Replace)", "Abbrechen")
        val result = JOptionPane.showOptionDialog(
            parent,
            "Die Datei enthält $importCount Kategorie(n).\n" +
            "Aktuell sind $existingCount Kategorie(n) vorhanden.\n\n" +
            "Wie soll importiert werden?",
            "Import-Modus wählen",
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
