# 🎣 MoripaFishing - Minecraft Fishing Plugin 🎣

A plugin that provides a richer and more customizable fishing experience on Minecraft servers. 🐟✨

## 🌟 Abstract

MoripaFishing is a Minecraft plugin that allows server administrators to freely configure their own fish, rarities, and fishing conditions (world, biome, weather, etc.). It manages player (Angler) fishing records and provides an extensible API.

## ⚙️ Main Features

*   **🐠 Custom Fish Definition:** Freely configure names, descriptions, rarities, and fishing conditions (world, biome, weather, time of day, etc.) in JSON files.
*   **💎 Custom Rarity:** Create your own rarity tiers (e.g., Common, Rare, Epic, Legendary, Mythic) with custom colors and probabilities.
*   **🌍 Condition-Based Fishing:** Set fish that can only be caught in specific worlds, biomes, weather conditions, or time periods.
*   **🎣 Player Records:** Track and manage fishing statistics for each player (future expansion).
*   **🔧 Easy Configuration:** Intuitive configuration through JSON files in the `plugins/MoripaFishing/` directory.
*   **💻 Developer API:** Integrate and extend functionality through the `api` module.
*   **🛠️ Admin Commands:** Commands to modify weather and world settings (e.g., `/mf weather`, `/mf world`).

## 📦 Module Structure

*   **`api`**: Provides core interfaces, data models, and events for the plugin. Used by other developers to integrate with MoripaFishing.
*   **`app`**: The main plugin implementation that runs on Minecraft servers, handling command processing and configuration file loading.

## ⌨️ Commands
[docs-commands](https://fishing.plugin.morino.party/category/-コマンド/)

## 👨‍💻 For Developers 🚀

### Environment 🌍

Install [Task](https://taskfile.dev/installation/) 💻
Install [Commitizen](https://commitizen.github.io/cz-cli/) 💻
```bash
sudo snap install task --classic
```

### Docs 📚

Open the docs directory and install the dependencies 🚧.
```bash
task docs
```

### Format 💪

Format the code 💻.
```bash
task format
```

### Build 🚧

Build the project 🚀.
```bash
task build
```

### Commit 📝

Install cz-emoji 💻
```bash
pnpm install --global cz-emoji
```

Commit the changes 💻.
```bash
task commit
```



