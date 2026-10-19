![WelcomeMat Logo](https://github.com/coffeeisle/welcome-mat/blob/main/assets/gh_header.png)

<div align="center">
  <h1>WelcomeMat</h1>
  <p>A friendly way to roll out the welcome mat for your players!</p>
  
  <p>
    <a href="https://modrinth.com/plugin/welcome-mat">
      <img alt="Modrinth Downloads" src="https://img.shields.io/modrinth/dt/welcome-mat?logo=modrinth&labelColor=34343c&color=00AF5C">
    </a>
    <a href="https://discord.gg/cJ4uP2xF7h">
      <img alt="Discord" src="https://img.shields.io/discord/813255312449601597?logo=discord&labelColor=34343c&color=5865F2">
    </a>
    <a href="https://github.com/coffeeisle/welcome-mat">
      <img alt="Modrinth Game Versions" src="https://img.shields.io/modrinth/game-versions/welcome-mat?color=00AF5C&label=Minecraft&logo=modrinth">
    </a>
    <a href="https://modrinth.com/plugin/welcome-mat">
      <img alt="Modrinth Version" src="https://img.shields.io/modrinth/v/welcome-mat?&color=blue&label=Version">
    </a>
  </p>
</div>

## 📥 Download

WelcomeMat is available on Modrinth! Click the button below to download:

<a href="https://modrinth.com/plugin/welcome-mat">
  <img alt="Download on Modrinth" src="https://avatars.githubusercontent.com/u/67560307" width="60">
</a>

## 🔄 Compatibility

WelcomeMat is compatible with Minecraft versions 1.13 through 26.3 (including 26.1.2, 26.2, and 26.3) and is fully tested on Bukkit/Spigot, Paper, Purpur, and Folia (regionized multithreading). Minecraft 26.1+ requires Java 25. The Folia build uses region-aware schedulers so the same jar runs safely on both single-threaded and regionized servers.

### 🚀 What's New in v1.4.0
- **Minecraft 26.x support:** Fully compatible with Minecraft 26.1.2, 26.2, and 26.3 on Bukkit/Spigot, Paper, Purpur, and Folia.
- **Java 25:** Updated build toolchain to Java 25 to match the Minecraft 26.1+ runtime requirement.
- **Particle data fix:** Confetti Burst now supplies color data for `ENTITY_EFFECT` particles, preventing `missing required data class org.bukkit.Color` errors on 26.x servers.

### 🚀 What's New in v1.3.3
- **Animation test polish:** `/wm animation test <name|random>` now cycles through every registered animation, making it easier to preview the expanded effect roster on live servers.
- **Confetti Burst fix:** Folia servers no longer spam `missing required data class org.bukkit.Color`—the Confetti Burst animation now supplies proper dust color transitions and sparkles.
- **Safe config migrations:** `config.yml` and `messages.yml` automatically back up and merge new defaults, so upgrading from older releases preserves customizations without YAML errors.

## ✨ Features

### Core Features
| Feature | Description | Status |
|---------|-------------|--------|
| Message Packs | Multiple themed welcome messages | ✅ Implemented |
| Welcome Titles | Customizable titles and subtitles | ✅ Implemented |
| Interactive Sounds | Configurable join/leave sounds | ✅ Implemented |
| Join Effects | Beautiful particle effects | ✅ Implemented |
| Language Support | Multiple language options | ✅ Implemented |
| User-Friendly GUI | Easy settings management | ✅ Implemented |

> New in v1.3.0: Skyward (Happy Ghast) and Copper Age message packs themed after the 1.21.10 content updates.

### Player Experience
| Feature | Description | Status |
|---------|-------------|--------|
| Custom Message Delays | Set timing for welcome messages | ✅ Implemented |
| Player-specific Messages | Personalized greetings based on player history | ✅ Implemented |
| Message Randomization | Random selection from message pool | ✅ Implemented |
| First Join Special Events | Unique celebrations for first-time players | ✅ Implemented |
| Multi-world Support | Different settings per world | 🚧 Planned |

### Administration
| Feature | Description | Status |
|---------|-------------|--------|
| Message Templates | Pre-made message templates | ✅ Implemented |
| Permissions System | Granular permission control | ✅ Implemented |
| Message Scheduling | Time-based welcome messages | ✅ Implemented |
| Statistics Tracking | Track player join/leave patterns | 🚧 Planned |

### Quality of Life
| Feature | Description | Status |
|---------|-------------|--------|
| Command Shortcuts | Quick commands for common actions | ✅ Implemented |
| Message Preview | Preview messages before saving | ✅ Implemented |
| Import/Export | Share configurations between servers | 🚧 Planned |
| Auto-updates | Automatic plugin updates | 🚧 Planned |
| Performance Optimization | Improved resource usage | ✅ Implemented |

## 🕹 Command Reference

| Command | Permission | Description |
|---------|------------|-------------|
| `/welcomemat` or `/wm` | (default) | Shows the interactive help panel with quick-click shortcuts.
| `/wm gui` | `welcomemat.config` | Opens the in-game Settings GUI for packs, languages, sound profiles, and effects.
| `/wm sound` | `welcomemat.sound.toggle` | Toggles join/leave sounds for the executing player.
| `/wm effects` | `welcomemat.effects` | Enables or disables join effects for the executing player.
| `/wm on` | `welcomemat.toggle` | Enables WelcomeMat for the executing player.
| `/wm off` | `welcomemat.toggle` | Disables WelcomeMat for the executing player.
| `/wm animation` | `welcomemat.animation` | Opens the animation status view; supports `list`, `set <id|pack|random>`, and `test <id|random>`.
| `/wm pack` | `welcomemat.pack` | Selects a message pack, switches pack modes, or creates new packs with `/wm pack create <Friendly Name>`.
| `/wm splash <title\|subtitle> [edit]` | `welcomemat.config` | Views or edits the welcome title/subtitle using clickable chat controls.
| `/wm language <english\|spanish>` | `welcomemat.language` | Switches the active language bundle.
| `/wm config <get\|set\|list>` | `welcomemat.config` | Reads or writes any config path. Sound paths preview audio instantly for players.
| `/wm reload` | `welcomemat.reload` | Reloads `config.yml`, `messages.yml`, and re-runs the migration helper.

### Command Examples

```mcfunction
# Preview the latest particle work without relogging
/wm animation test random

# Force the Copper Age message pack while keeping custom splash text
/wm pack copper_age
/wm pack mode splash custom

# Update the default join sound and immediately preview it
/wm config set join-sound ENTITY_PLAYER_LEVELUP
/wm config set join-sound-pitch 1.3
```

## 📚 Documentation

For full documentation, examples, and setup instructions, please visit our [Modrinth page](https://modrinth.com/plugin/welcome-mat).

## 🤝 Contributing

Contributions are welcome! Feel free to:
1. Report bugs
2. Suggest new features
3. Submit pull requests

## 📝 Support

Need help? Have suggestions?
- Create an [issue](https://github.com/coffeeisle/welcome-mat/issues)
- Join our [Discord server](https://discord.gg/cJ4uP2xF7h)

## 📜 License

WelcomeMat is licensed under the [MIT License](LICENSE).

---

Made with ❤️ & ☕ by [angeldev0](https://github.com/4ngel2769)
