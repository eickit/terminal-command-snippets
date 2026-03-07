# Terminal Snippets – PhpStorm Plugin

Kategorisierte Terminal-Befehlsschnipsel direkt im PhpStorm-Terminal.

## Features

- **Snippets verwalten** unter *File → Settings → Tools → Terminal Snippets*
- **Kategorien** (Symfony, Composer, WP-CLI, Git, …) frei konfigurierbar
- **Toolbar-Button** im Terminal öffnet ein kategorisiertes Auswahlmenü
- Befehl wird **ohne Enter** ins Terminal eingetippt → du kannst ihn noch anpassen

---

## Voraussetzungen

| Tool | Version |
|------|---------|
| JDK  | 17+     |
| Gradle | 8.x (wird via Wrapper heruntergeladen) |
| IntelliJ IDEA (Community/Ultimate) | für den Build |
| PhpStorm (Ziel-IDE) | 2023.3 – 2024.x |

---

## Projekt bauen

### 1. Gradle Wrapper initialisieren (einmalig)

```bash
# Im Projektordner ausführen
gradle wrapper
```

Oder lade den Wrapper manuell herunter:
```bash
# Alternativ: Gradle direkt nutzen
gradle buildPlugin
```

### 2. Plugin bauen

```bash
./gradlew buildPlugin
```

Die fertige `.zip`-Datei liegt danach unter:
```
build/distributions/terminal-snippets-1.0.0.zip
```

### 3. Plugin in PhpStorm installieren

1. PhpStorm öffnen
2. *File → Settings → Plugins*
3. Zahnrad-Icon → **Install Plugin from Disk…**
4. Die erzeugte `.zip`-Datei auswählen
5. PhpStorm neu starten

---

## Verwendung

### Snippets verwalten

*File → Settings → Tools → Terminal Snippets*

```
+--------------------+------------------------------------------+
| Kategorien         | Snippets in "Symfony"                    |
| +--------------+   | +------------------+--------------------+ |
| | Symfony  (10)|   | | Name             | Befehl             | |
| | Composer  (6)|   | +------------------+--------------------+ |
| | WP CLI   (10)|   | | Cache Clear      | php bin/console …  | |
| | Git       (6)|   | | Make Controller  | php bin/console …  | |
| +--------------+   | +------------------+--------------------+ |
| [+] [✎] [-]        | [+] [✎] [-]                              |
+--------------------+------------------------------------------+
```

- **+** Neue Kategorie / neues Snippet hinzufügen
- **✎** Umbenennen / Bearbeiten
- **–** Löschen

### Snippet einfügen

1. Terminal in PhpStorm öffnen (*Alt+F12*)
2. Auf den **Snippets-Button** in der Terminal-Toolbar klicken
3. Kategorie → Befehl auswählen
4. Befehl erscheint im Terminal → bei Bedarf anpassen, dann *Enter* drücken

---

## Kompatibilität

| PhpStorm | Status |
|----------|--------|
| 2023.3.x | ✅ getestet |
| 2024.1.x | ✅ getestet |
| 2024.2.x | ✅ getestet |
| 2024.3.x | ⚠️ bitte testen (neue Terminal-Engine) |

> **Hinweis zur neuen Terminal-Engine (Gen2):** Ab PhpStorm 2024.2 gibt es eine neue
> experimentelle Terminal-Engine. Falls das Einfügen nicht funktioniert, deaktiviere
> *Settings → Tools → Terminal → Enable new terminal* und starte PhpStorm neu.

---

## Projektstruktur

```
terminal-snippets/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/wrapper/gradle-wrapper.properties
└── src/main/
    ├── kotlin/com/terminalsnippets/
    │   ├── model/
    │   │   └── SnippetData.kt          # Datenklassen
    │   ├── settings/
    │   │   ├── TerminalSnippetsState.kt          # Persistenz
    │   │   ├── TerminalSnippetsConfigurable.kt   # Settings-Seite
    │   │   ├── TerminalSnippetsSettingsPanel.kt  # Settings-UI
    │   │   ├── SnippetEditDialog.kt              # Snippet-Dialog
    │   │   └── CategoryEditDialog.kt             # Kategorie-Dialog
    │   ├── actions/
    │   │   └── TerminalSnippetsAction.kt         # Toolbar-Button + Popup-Menü
    │   └── util/
    │       └── TerminalHelper.kt                 # Text ins Terminal einfügen
    └── resources/META-INF/
        └── plugin.xml
```

---

## Lizenz

MIT – frei verwendbar und anpassbar.
