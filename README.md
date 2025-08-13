![GitHub release (latest by date)](https://img.shields.io/github/v/release/quangdev05/GiftCode24)
![GitHub license](https://img.shields.io/github/license/quangdev05/GiftCode24)
![Supported server version](https://img.shields.io/badge/Minecraft-1.13x%20--_1.21x-green)
[![Discord](https://img.shields.io/discord/1247029974154612828.svg?label=&logo=discord&logoColor=ffffff&color=7389D8&labelColor=6A7EC2)](https://discord.gg/4SGhHNmhE8)

# GiftCode24 Plugin
**GiftCode24** is a powerful gift code management plugin for Minecraft. It allows admins to create, manage and use gift codes with configurable options such as maximum usage, expiry date and player-specific limits. Features include creating, deleting, enabling, disabling, listing and assigning gift codes. GiftCode24 makes gift code management simple and efficient.

# [GiftCode24F Plugin](https://github.com/quangdev05/GiftCode24F/)
**GiftCode24F** is a support version for Folia fork with full functionality similar to the original version, recoded separately to best optimize for Folia fork.
### **Note:** The original version from version 3.0.0 has been supported for use on servers running fork Folia, so GiftCode24F will stop updating.

## Installation Instructions
1. **Download and install plugins**
- Download plugins on official platforms.
- Drag the Plugins file into the Plugins section in the server file.
2. **Activate Plugins**
- Restart the server or use PlugMan to activate.

## Command List
### Command List for Admin (/giftcode, /gc) [giftcode.admin]
- `/gc help`: Show command list
- `/gc create <code>`: Create a gift code.
- `/gc create <code> -g`: Create and open GUI to set item rewards
- `/gc create <base> -r [amount]`: Generate random codes (default 10)
- `/gc create <base> -r [amount] -c <template>`: Random codes using <template>'s
- `/gc guie <code>`: Open item GUI editor for a code
- `/gc setperm <code> <permission|none>`: Set/clear required permission for a code
- `/gc del <code>`: Delete gift code.
- `/gc reload`: Reload the Plugin.
- `/gc enable <code>`: Enable a gift code.
- `/gc disable <code>`: Disable a gift code.
- `/gc list`: List all gift codes.
- `/gc assign <code> <player>`: Assign a gift code to a player.
### Command List for Player (/code) [giftcode.player]
- `/code <code>`: Enter gift code.

## Infomation
- **Author:** QuangDev05 [GnauQ]
- **Facebook:** [Pham Quang](https://www.facebook.com/quangdev05)
- **Discord:** quangdev05
- **Community Discord:** [HyperFast Studio](https://discord.gg/4SGhHNmhE8)
- **Spigot:** [GiftCode24 on Spigot](https://www.spigotmc.org/resources/giftcode24.117453/)
- **BuiltByBit:** [GiftCode24 on BuiltByBit (v2.2.2-viStable is the last update)](https://builtbybit.com/resources/giftcode24.46671/)
