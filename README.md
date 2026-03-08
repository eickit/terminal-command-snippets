# Terminal Command Snippets – PhpStorm Plugin

Manage and insert categorized terminal commands directly from the PhpStorm terminal toolbar.

## Features

- **Manage snippets** under *File → Settings → Tools → Terminal Command Snippets*
- **Categories** (Symfony, Composer, WP-CLI, Git, …) – fully customizable
- **Dropdown button** in the terminal toolbar opens a categorized popup menu
- **Keyboard shortcut** Alt+Shift+S opens the snippet menu from anywhere
- Commands are inserted **without Enter** – you can still edit them before executing
- **Highlight** individual snippets to display them in **bold** in the menu
- **Import/Export** all categories and snippets as JSON backup
- Pre-defined categories: Symfony, Composer, WP-CLI, Git

---

## Requirements

| Tool | Version |
|------|---------|
| JDK | 17+ |
| Gradle | 8.x (downloaded automatically via wrapper) |
| IntelliJ IDEA (Community/Ultimate) | for building |
| PhpStorm (target IDE) | 2023.3 or later |

---

## Building

### 1. Initialize Gradle Wrapper (once)

```bash
gradle wrapper
```

### 2. Build the plugin

```bash
./gradlew buildPlugin
```

The resulting `.zip` file will be located at:
```
build/distributions/terminal-snippets-1.0.0.zip
```

### 3. Install in PhpStorm

1. Open PhpStorm
2. Go to *File → Settings → Plugins*
3. Click the gear icon → **Install Plugin from Disk…**
4. Select the generated `.zip` file
5. Restart PhpStorm

---

## Usage

### Managing snippets

*File → Settings → Tools → Terminal Command Snippets*

```
+--------------------+-----------------------------------------------+
| Categories         | Snippets in "Symfony"                         |
| +--------------+   | +---+------------------+---------------------+ |
| | Symfony  (10)|   | | ★ | Name             | Command             | |
| | Composer  (6)|   | +---+------------------+---------------------+ |
| | WP CLI   (10)|   |     | Cache Clear      | php bin/console …   | |
| | Git       (6)|   |  ★  | Make Controller  | php bin/console …   | |
| +--------------+   | +---+------------------+---------------------+ |
| [+] [✎] [-]        | [+] [✎] [-]          [Import…]  [Export…]   |
+--------------------+-----------------------------------------------+
```

- **+** Add a new category or snippet
- **✎** Rename / edit
- **–** Delete
- **★** Mark a snippet as highlighted (shown in bold in the menu)
- **Import…** / **Export…** Load or save all snippets as a JSON file

### Inserting a snippet

1. Open the terminal in PhpStorm (*Alt+F12*)
2. Click the **Terminal Command Snippets** button in the terminal toolbar (or press *Alt+Shift+S*)
3. Select a category, then a command
4. The command appears in the terminal – edit if needed, then press *Enter*

### Snippet dialog fields

| Field | Description |
|-------|-------------|
| Name | Display name shown in the menu |
| Command | The terminal command to insert |
| Description | Optional short description shown next to the name |
| Highlight in menu (bold) | If checked, the snippet name appears bold in the dropdown |

---

## Compatibility

| PhpStorm | Status |
|----------|--------|
| 2023.3.x | ✅ tested |
| 2024.1.x | ✅ tested |
| 2024.2.x | ✅ tested |
| 2025.1.x | ✅ tested |

> **Note on the new terminal engine (Gen2):** Starting with PhpStorm 2024.2, a new terminal engine
> is available. If command insertion does not work automatically, the command is copied to your
> clipboard – just paste manually with ⌘V / Ctrl+V.

---

## Project structure

```
terminal-snippets/
├── build.gradle.kts
├── settings.gradle.kts
├── LICENSE
├── gradle/wrapper/gradle-wrapper.properties
└── src/main/
    ├── kotlin/com/terminalsnippets/
    │   ├── model/
    │   │   └── SnippetData.kt                    # Data models
    │   ├── settings/
    │   │   ├── TerminalSnippetsState.kt           # Persistent state
    │   │   ├── TerminalSnippetsConfigurable.kt    # Settings page registration
    │   │   ├── TerminalSnippetsSettingsPanel.kt   # Settings UI
    │   │   ├── SnippetEditDialog.kt               # Add/edit snippet dialog
    │   │   ├── CategoryEditDialog.kt              # Add/rename category dialog
    │   │   └── ImportExportService.kt             # JSON import/export
    │   ├── actions/
    │   │   ├── SnippetActionGroup.kt              # Toolbar dropdown group
    │   │   ├── InjectSnippetAction.kt             # Single snippet menu entry
    │   │   └── OpenSnippetsPopupAction.kt         # Keyboard shortcut action
    │   └── util/
    │       └── TerminalHelper.kt                  # Clipboard + paste logic
    └── resources/META-INF/
        └── plugin.xml
```

---

## License

[MIT](LICENSE) – free to use and modify.
