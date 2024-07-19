![GitHub release (latest by date)](https://img.shields.io/github/v/release/QuangDev05/GiftCode24)
![GitHub license](https://img.shields.io/github/license/PhamQuang2008/GiftCode)
![Supported server version](https://img.shields.io/badge/minecraft-1.12x%20--_Latest-green)
[![Discord](https://img.shields.io/discord/1247029974154612828.svg?label=&logo=discord&logoColor=ffffff&color=7389D8&labelColor=6A7EC2)](https://discord.gg/HsSUVGSc3c)

# GiftCode24 Plugin

**GiftCode24** is a Minecraft plugin that allows you to manage and use gift codes on your server. The plugin provides commands for admins to create, enable/disable, and reload gift codes, as well as commands for players to redeem codes and receive rewards.

## Features

- **Gift Code Management:** Create, delete, enable, and disable gift codes.
- **Code Redemption:** Players can use the `/code <code>` command to redeem a gift code.
- **Customizable Rewards:** Configure commands and messages for each gift code.
- **Automatic Update Checking:** The plugin automatically checks for updates and notifies admins.

## Installation

1. **Download and Install the Plugin**
   - Download the plugin from [SpigotMC](https://www.spigotmc.org/resources/giftcode24.117453/).
   - Place the JAR file into the `plugins` folder of your Minecraft server.

2. **Start the Server**
   - Start the server to allow the plugin to create its configuration files.

3. **Configure the Plugin**
   - Open `config.yml` and `giftcode.yml` to customize settings and gift code configurations.

## Commands

### For Admins

- `/giftcode help`: Display help for plugin commands.
- `/giftcode create <code>`: Create a new gift code.
- `/giftcode create <name> random`: Generate 10 random gift codes with a base name.
- `/giftcode del <code>`: Delete a gift code.
- `/giftcode reload`: Reload the configuration file.
- `/giftcode enable <code>`: Enable a gift code.
- `/giftcode disable <code>`: Disable a gift code.
- `/giftcode list`: List all created gift codes.
- `/giftcode <code> <player>`: Assign a gift code to a specified player.

### For Players

- `/code <code>`: Redeem a gift code.

## Configuration Examples

### config.yml

```yaml
# Plugin made by QuangDev05
# Plugin is already at version '1.2.0 | Stable'
# Facebook: https://www.facebook.com/quangdev05.2024
# Discord: quangdev05
# Discord Community: https://discord.gg/MdgvJnegbM
# Github: https://github.com/QuangDev05/GiftCode24
# Spigot: https://www.spigotmc.org/resources/giftcode24.117453/
# BuiltByBit: https://builtbybit.com/resources/giftcode24.46671/

update-checker:
  enabled: true  # Set to false to disable automatic update checking

messages:
  invalid-code: "The gift code you entered is invalid."
  expired-code: "The gift code you entered has expired."
  max-uses-reached: "The gift code you entered has reached its maximum number of uses."
  code-disabled: "The gift code you entered is currently disabled."
  code-redeemed: "You have successfully redeemed the gift code!"
  code-already-redeemed: "You have already redeemed this code."
```

### giftcode.yml 

```yaml
# Gift Code Configuration File
# This file defines settings for managing gift codes in the plugin.

# Example of a gift code entry:
samplecode:
  commands:
    - give %player% diamond 1  # Command to give the player 1 diamond when redeeming the code.
  message: "You have received 1 diamond!"  # Message displayed to the player upon redeeming the code.
  max-uses: 10  # Maximum number of times this code can be redeemed in total.
  expiry: "2024-12-31T23:59:59"  # Expiration date and time for the code (ISO 8601 format).
  enabled: true  # Whether the code is currently enabled and can be redeemed.
  player-max-uses: 1  # Maximum number of times each player can redeem this code. Set to -1 for unlimited uses.
```

## Contact

- **Author:** QuangDev05
- **Facebook:** [Phạm Quang](https://www.facebook.com/quangdev05)
- **Discord:** quangdev05
- **Discord Community:** [Join our Discord](https://discord.gg/HsSUVGSc3c)
- **Github:** [PhamQuang2008/GiftCode](https://github.com/QuangDev05/GiftCode)
- **Spigot:** [GiftCode24 on Spigot](https://www.spigotmc.org/resources/giftcode24.117453/)
- **BuiltByBit:** [GiftCode24 on BuiltByBit](https://builtbybit.com/resources/giftcode24.46671/)

## Notes

- This plugin allows server admins to manage and use gift codes in Minecraft.
- Admins can create, enable/disable, and reload gift codes using commands.
- Players can redeem gift codes to receive rewards.
- Supports automatic update checking.


