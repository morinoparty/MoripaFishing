# MoripaFishing

A fishing plugin for Minecraft servers. Fish, rarities, weather, time of day, and world conditions are all defined via JSON, letting server admins build their own custom fishing experience.

## Features

- **Custom fish definitions**: Configure name, description, rarity, and catch conditions (world, biome, weather, time of day, etc.) in JSON
- **Custom rarities**: Define any tier (Common, Rare, Epic, Legendary, ...) with its own color and catch probability
- **Conditional fishing**: Restrict fish to specific worlds, biomes, weather, or time periods
- **Extensible API**: Extend functionality from external plugins (Integrations / Addons) via the `api` module
- **Admin commands**: Manage weather and world settings (`/mf weather`, `/mf world`, etc.)

See the [documentation site](https://fishing.plugin.morino.party/category/-コマンド/) for the full command list.

Release artifacts are signed with GitHub Attestations, so their SLSA provenance can be verified. Run `gh attestation verify <jar-file> --owner morinoparty` to check.

## Modules

| Module | Description |
|--------|-------------|
| `api` | Public interfaces, models, config schemas, and events |
| `bukkit` | Main plugin implementation (commands, listeners, managers) |
| `integrations/weather` | Add-on plugin for weather control |
| `integrations/world-lifecycle` | Add-on plugin for world generation integration |
| `addons/catch-announce` | Catch-announcement feature split out as an Addon |
| `docs` | Documentation site built with Fumadocs (Next.js) |
| `buildSrc` | Gradle helper tasks and build logic |

## Tech stack

- **Kotlin** 2.4 / **Java** 25 (Temurin)
- **Paper API** - Minecraft server API
- **Cloud** - command framework (Incendo)
- **Koin** - dependency injection
- **Arrow** - functional programming
- **kotlinx.serialization / KAML** - config file serialization
- **JUnit 5 / MockK / MockBukkit** - testing
- **Fumadocs** - documentation site (Next.js)

## Requirements

- **Java** 25 (Temurin recommended)
- **Gradle** 9.x (wrapper included)
- **Node.js** 22+ / **pnpm** 10+ (for building the docs)
- **[Task](https://taskfile.dev/)** (task runner, optional)

```bash
sudo snap install task --classic
```

## Build & test

```bash
# Gradle build
./gradlew build

# or with Task
task build
task test
task check   # ktlintFormat + detekt + test
```

## Run a dev server

```bash
./gradlew runServer

# or with Task
task run
```

## Docs development

```bash
cd docs
pnpm install
pnpm dev

# or with Task
task docs
```

## Task commands

| Command | Description |
|---------|-------------|
| `task build` | Build all modules |
| `task test` | Run tests |
| `task check` | Format + lint + test |
| `task run` | Start a dev server |
| `task docs` | Start the docs dev server |
| `task dokka` | Generate Dokka API docs |
| `task commit` | Format, then create a commit message (`git-cz`) |
| `task unlock` | Remove the `session.lock` file |

## License

Written in 2025-2026 by Morinoparty developer team. No Rights Reserved.

To the extent possible under law, morinoparty has waived all copyright and related or neighboring rights to MoripaFishing. This work is published from: Japan.

You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see http://creativecommons.org/publicdomain/zero/1.0/.

This CC0 dedication applies to the source code only. Non-code assets (images, icons, logos, and other media) are **not** covered and remain under their respective rights unless stated otherwise.
