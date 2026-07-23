# Scoreboard Editor

A **client-side Fabric mod** for Minecraft 1.21.1 that lets you visually customize any server's sidebar scoreboard — text, numbers, colours, position, scale, and opacity — without touching the server or sending any fake packets.

Built primarily for **DonutSMP**, but works with any server that uses the vanilla sidebar scoreboard.

---

## Features

| Feature | Details |
|---|---|
| **Rule-based text replacement** | Match any scoreboard line (exact or `*` wildcard), replace text and/or score display |
| **Formatting codes** | `&a`, `&l`, `&c` … all standard Minecraft colour/format codes in replacements |
| **Hide lines** | Leave the replacement blank to hide a line entirely |
| **Live updates** | Rules are re-applied every render frame; server scoreboard changes don't break your edits |
| **Profiles** | Multiple named profiles (Default, DonutSMP, Hypixel) — switch instantly |
| **Layout controls** | X/Y offset, scale, line spacing, background opacity, text shadow, hide scores |
| **Rebindable hotkey** | Default `` ` `` (backtick) — change it in Controls |

---

## Requirements

- Minecraft Java Edition **1.21.1**
- [Fabric Loader](https://fabricmc.net/use/installer/) **≥ 0.16.0**
- [Fabric API](https://modrinth.com/mod/fabric-api) **0.102.0+1.21.1**
- Java **21**

---

## Building

### 1 — Install the Gradle wrapper (first time only)

```bash
gradle wrapper --gradle-version 8.8
```

Or download it from the [Gradle releases page](https://gradle.org/releases/) and place the files:

```
gradlew
gradlew.bat
gradle/wrapper/gradle-wrapper.jar
gradle/wrapper/gradle-wrapper.properties
```

### 2 — Build

```bash
./gradlew build
```

The compiled `.jar` will be in `build/libs/`. Use the one **without** `-sources` in the name.

### 3 — Install

Copy the `.jar` into your Minecraft `mods/` folder alongside Fabric API.

---

## Project Structure

```
scoreboard-editor/
├── build.gradle
├── settings.gradle
├── gradle.properties
├── src/
│   ├── main/
│   │   └── resources/
│   │       └── fabric.mod.json          ← mod metadata (canonical location)
│   └── client/
│       ├── java/com/scoreboardeditor/
│       │   ├── ScoreboardEditorMod.java ← client entry-point
│       │   ├── config/                  ← ModConfig + ConfigManager (Gson JSON)
│       │   ├── profile/                 ← Profile + ProfileManager
│       │   ├── scoreboard/              ← LineRule, ScoreboardState, ScoreboardCapture
│       │   ├── manager/                 ← RuleManager (applies rules each frame)
│       │   ├── render/                  ← ScoreboardLayout (resolved per-frame layout)
│       │   ├── hud/                     ← CustomScoreboardRenderer (HUD drawing)
│       │   ├── mixin/                   ← InGameHudMixin (cancels vanilla rendering)
│       │   ├── input/                   ← KeybindHandler
│       │   ├── gui/                     ← ScoreboardEditorScreen (tabbed editor UI)
│       │   │   └── components/          ← RuleListWidget, LineEditorEntry
│       │   └── util/                    ← TextUtil, ColorUtil
│       └── resources/
│           ├── scoreboard-editor.mixins.json
│           └── assets/scoreboard-editor/lang/en_us.json
```

> **Note:** `src/client/resources/fabric.mod.json` was generated in error — delete it.
> The canonical `fabric.mod.json` lives in `src/main/resources/`.

---

## Config files (auto-generated)

| File | Location | Contents |
|---|---|---|
| `scoreboard-editor.json` | `.minecraft/config/` | Global settings (enabled, offset, scale, …) |
| `scoreboard-editor-profiles.json` | `.minecraft/config/` | All profiles and their rules |

---

## Writing Rules

Open the editor with `` ` ``, switch to the **Rules** tab.

| Field | Description |
|---|---|
| **Match** | Plain text to find in the scoreboard line. Use `*` as a wildcard. Case-insensitive. |
| **Replace** | Text to display instead. Use `&a`–`&f` for colour, `&l` bold, `&o` italic, `&r` reset. Leave blank to hide the line. |
| **Score Override** | Optional number/text to show in the score column instead of the real value. |
| **ON / OFF** | Toggle a rule without deleting it. |

### Examples

| Match | Replace | Result |
|---|---|---|
| `balance: *` | `Balance: &a$25B` | Replaces any balance line with green custom text |
| `kills: *` | `Kills: &c42` | Red kills count |
| `deaths: *` | *(blank)* | Hides the deaths line entirely |
| `www.hypixel.net` | `&bhypixel.net` | Aqua server URL |

---

## DonutSMP Compatibility

The built-in **DonutSMP** profile ships with preset rules for common DonutSMP scoreboard lines.  
Activate it from the **Profiles** tab.

Because rules are matched against the **plain text** of the line (colour codes stripped) and re-applied every render frame, the mod handles DonutSMP's rapid scoreboard updates transparently — your custom display is always up to date.

---

## Architecture Notes

- **No packets sent.** The mod only reads the scoreboard, never writes to it.
- **No server impact.** Everything is local rendering only.
- **Rendering pipeline:** `InGameHudMixin` cancels `InGameHud.renderScoreboardSidebar`; `CustomScoreboardRenderer` draws the custom version at the same call-site.
- **Rule application:** `RuleManager.apply()` is called every frame (cheap — it only iterates ~15 lines and a handful of rules).
- **Config:** Gson serialization to `config/` directory; saved on screen close and on every rule/profile change.
