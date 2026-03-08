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
 * Persists all categories and snippets in terminalSnippets.xml
 * inside the IDE configuration directory.
 */
@State(
    name = "TerminalSnippetsState",
    storages = [Storage("terminalSnippets.xml")]
)
@Service(Service.Level.APP)
class TerminalSnippetsState : PersistentStateComponent<TerminalSnippetsState> {

    var categories: MutableList<SnippetCategory> = mutableListOf(
        // Symfony – default snippets
        SnippetCategory("Symfony").apply {
            snippets.addAll(
                listOf(
                    TerminalSnippet("Console", "php bin/console", "Open Symfony Console"),
                    TerminalSnippet("Cache Clear", "php bin/console cache:clear", "Clear the cache"),
                    TerminalSnippet("Cache Warmup", "php bin/console cache:warmup", "Warm up the cache"),
                    TerminalSnippet("Make Controller", "php bin/console make:controller", "Create a controller"),
                    TerminalSnippet("Make Entity", "php bin/console make:entity", "Create an entity"),
                    TerminalSnippet("Make Migration", "php bin/console make:migration", "Create a migration"),
                    TerminalSnippet("Migrate", "php bin/console doctrine:migrations:migrate", "Run migrations"),
                    TerminalSnippet("Routes List", "php bin/console debug:router", "List all routes"),
                    TerminalSnippet("Server Start", "symfony server:start", "Start the dev server"),
                    TerminalSnippet("Server Stop", "symfony server:stop", "Stop the dev server")
                )
            )
        },

        // Composer
        SnippetCategory("Composer").apply {
            snippets.addAll(
                listOf(
                    TerminalSnippet("Install", "composer install", "Install dependencies"),
                    TerminalSnippet("Update", "composer update", "Update dependencies"),
                    TerminalSnippet("Require", "composer require ", "Add a package (append name)"),
                    TerminalSnippet("Remove", "composer remove ", "Remove a package (append name)"),
                    TerminalSnippet("Dump Autoload", "composer dump-autoload", "Regenerate the autoloader"),
                    TerminalSnippet("Outdated", "composer outdated", "List outdated packages")
                )
            )
        },

        // WP-CLI
        SnippetCategory("WP CLI").apply {
            snippets.addAll(
                listOf(
                    TerminalSnippet("Plugin List", "wp plugin list", "List all plugins"),
                    TerminalSnippet("Plugin Update All", "wp plugin update --all", "Update all plugins"),
                    TerminalSnippet("Theme List", "wp theme list", "List all themes"),
                    TerminalSnippet("Core Update", "wp core update", "Update WordPress core"),
                    TerminalSnippet("Cache Flush", "wp cache flush", "Flush the WordPress cache"),
                    TerminalSnippet("DB Export", "wp db export backup.sql", "Export the database"),
                    TerminalSnippet("DB Import", "wp db import backup.sql", "Import the database"),
                    TerminalSnippet("Search Replace", "wp search-replace 'old' 'new'", "Search & replace in DB"),
                    TerminalSnippet("User List", "wp user list", "List all users"),
                    TerminalSnippet("Cron Event List", "wp cron event list", "List cron jobs")
                )
            )
        },

        // Git
        SnippetCategory("Git").apply {
            snippets.addAll(
                listOf(
                    TerminalSnippet("Status", "git status", "Show repository status"),
                    TerminalSnippet("Pull", "git pull", "Pull latest changes"),
                    TerminalSnippet("Add All", "git add -A", "Stage all changes"),
                    TerminalSnippet("Commit", "git commit -m \"\"", "Create a commit (add message)"),
                    TerminalSnippet("Push", "git push", "Push changes"),
                    TerminalSnippet("Log", "git log --oneline -20", "Show last 20 commits")
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
