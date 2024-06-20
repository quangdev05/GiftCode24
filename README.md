![GitHub release (latest by date)](https://img.shields.io/github/v/release/PhamQuang2008/GiftCode)
![GitHub license](https://img.shields.io/github/license/PhamQuang2008/GiftCode)
![Supported server version](https://img.shields.io/badge/minecraft-1.12x%20--_Latest-green)
[![Discord](https://img.shields.io/discord/1247029974154612828.svg?label=&logo=discord&logoColor=ffffff&color=7389D8&labelColor=6A7EC2)](https://discord.gg/HsSUVGSc3c)

# GiftCode24

GiftCode24 is a Minecraft plugin that allows you to manage and use gift codes on your Minecraft server. The plugin provides commands for administrators to create, enable/disable, and reload gift codes, as well as commands for players to redeem codes and receive rewards.

## Key Features

- Create and manage gift codes with options such as maximum uses, expiration time, and executable commands.
- Flexible configuration through `config.yml` and `giftcode.yml` files.
- Provides notifications and detailed information when players redeem gift codes.
- Supports configuration reload without restarting the server.

## Basic Commands

### Administrator Commands (/giftcode)

- `/giftcode create <code>`: Create a new gift code.
- `/giftcode create <name> random`: Generate a batch of 10 random gift codes.
- `/giftcode del <code>`: Delete a gift code.
- `/giftcode reload`: Reload the gift code configuration file.
- `/giftcode enable <code>`: Enable a gift code.
- `/giftcode disable <code>`: Disable a gift code.
- `/giftcode list`: List all created gift codes.
- `/giftcode <code> <player>`: Assign a gift code to a specified player.

### Player Command (/code)

- `/code <code>`: Redeem a gift code to receive the corresponding reward.

## Installation

1. Download the plugin from [SpigotMC](https://www.spigotmc.org/resources/giftcode24.117453/) or [GitHub](https://github.com/PhamQuang2008/GiftCode).
2. Place the JAR file into the plugins folder of your Minecraft server.
3. Restart the server to load and start the plugin.

## Configuration

The main configuration files are `config.yml` and `giftcode.yml`, allowing you to customize settings such as messages, code expiration, and more.

## Feedback

If you encounter issues or have new feature requests for the plugin, please create an issue on GitHub or contact directly.

## Contributions

Contributions to improve the plugin are welcome. Feel free to create pull requests on GitHub for any enhancements.

---

**Author**: QuangDev05  
**Version**: 1.1.0 | Stable  
**Discord**: quangdev05  
**Facebook**: [QuangDev05](https://www.facebook.com/quangdev05.2024)

