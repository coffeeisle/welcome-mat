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

WelcomeMat is compatible with Minecraft versions 1.13 through 1.21.10 and is fully tested on Bukkit/Spigot, Paper, Purpur, and Folia (regionized multithreading). The Folia build uses region-aware schedulers so the same jar runs safely on both single-threaded and regionized servers.

### 🚀 What's New in v1.3.1
- **Folia-safe animations:** Join effects now use the latest Folia entity schedulers, keeping animations responsive without tripping the watchdog on 1.21.8+.
- **Smarter fallbacks:** If Folia ever exposes a new scheduler signature, the plugin logs the discovery and skips unsafe fallbacks instead of touching Bukkit's global scheduler.
- **Automatic version metadata:** `plugin.yml` now receives the Gradle project version at build time, so downloads are always correctly labeled.

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
