package com.terminalsnippets.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import com.terminalsnippets.model.SnippetCategory
import com.terminalsnippets.model.TerminalSnippet

/**
 * Speichert alle Kategorien und Snippets dauerhaft in terminalSnippets.xml
 * im IDE-Konfigurationsverzeichnis.
 */
@State(
    name = "TerminalSnippetsState",
    storages = [Storage("terminalSnippets.xml")]
)
@Service(Service.Level.APP)
class TerminalSnippetsState : PersistentStateComponent<TerminalSnippetsState> {

    var categories: MutableList<SnippetCategory> = mutableListOf(
        // Symfony – Standard-Snippets
        SnippetCategory("Symfony").apply {
            snippets.addAll(
                listOf(
                    TerminalSnippet("Console", "php bin/console", "Symfony Console aufrufen"),
                    TerminalSnippet("Cache Clear", "php bin/console cache:clear", "Cache leeren"),
                    TerminalSnippet("Cache Warmup", "php bin/console cache:warmup", "Cache aufwärmen"),
                    TerminalSnippet("Make Controller", "php bin/console make:controller", "Controller erstellen"),
                    TerminalSnippet("Make Entity", "php bin/console make:entity", "Entity erstellen"),
                    TerminalSnippet("Make Migration", "php bin/console make:migration", "Migration erstellen"),
                    TerminalSnippet("Migrate", "php bin/console doctrine:migrations:migrate", "Migrationen ausführen"),
                    TerminalSnippet("Routes List", "php bin/console debug:router", "Alle Routen anzeigen"),
                    TerminalSnippet("Server Start", "symfony server:start", "Dev-Server starten"),
                    TerminalSnippet("Server Stop", "symfony server:stop", "Dev-Server stoppen")
                )
            )
        },

        // Composer
        SnippetCategory("Composer").apply {
            snippets.addAll(
                listOf(
                    TerminalSnippet("Install", "composer install", "Dependencies installieren"),
                    TerminalSnippet("Update", "composer update", "Dependencies aktualisieren"),
                    TerminalSnippet("Require", "composer require ", "Paket hinzufügen (Name ergänzen)"),
                    TerminalSnippet("Remove", "composer remove ", "Paket entfernen (Name ergänzen)"),
                    TerminalSnippet("Dump Autoload", "composer dump-autoload", "Autoloader neu generieren"),
                    TerminalSnippet("Outdated", "composer outdated", "Veraltete Pakete anzeigen")
                )
            )
        },

        // WP-CLI
        SnippetCategory("WP CLI").apply {
            snippets.addAll(
                listOf(
                    TerminalSnippet("Plugin List", "wp plugin list", "Alle Plugins auflisten"),
                    TerminalSnippet("Plugin Update All", "wp plugin update --all", "Alle Plugins aktualisieren"),
                    TerminalSnippet("Theme List", "wp theme list", "Alle Themes auflisten"),
                    TerminalSnippet("Core Update", "wp core update", "WordPress aktualisieren"),
                    TerminalSnippet("Cache Flush", "wp cache flush", "WordPress-Cache leeren"),
                    TerminalSnippet("DB Export", "wp db export backup.sql", "Datenbank exportieren"),
                    TerminalSnippet("DB Import", "wp db import backup.sql", "Datenbank importieren"),
                    TerminalSnippet("Search Replace", "wp search-replace 'alt' 'neu'", "Suchen & Ersetzen in DB"),
                    TerminalSnippet("User List", "wp user list", "Benutzer auflisten"),
                    TerminalSnippet("Cron Event List", "wp cron event list", "Cron-Jobs anzeigen")
                )
            )
        },

        // Git
        SnippetCategory("Git").apply {
            snippets.addAll(
                listOf(
                    TerminalSnippet("Status", "git status", "Repository-Status anzeigen"),
                    TerminalSnippet("Pull", "git pull", "Änderungen pullen"),
                    TerminalSnippet("Add All", "git add -A", "Alle Änderungen stagen"),
                    TerminalSnippet("Commit", "git commit -m \"\"", "Commit erstellen (Message ergänzen)"),
                    TerminalSnippet("Push", "git push", "Änderungen pushen"),
                    TerminalSnippet("Log", "git log --oneline -20", "Letzten 20 Commits anzeigen")
                )
            )
        }
    )

    override fun getState(): TerminalSnippetsState = this

    override fun loadState(state: TerminalSnippetsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(): TerminalSnippetsState =
            ApplicationManager.getApplication().getService(TerminalSnippetsState::class.java)
    }
}
