package quangdev05.giftcode;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class GiftCode extends JavaPlugin {

    private FileConfiguration config;
    private File configFile;

    @Override
    public void onEnable() {
        createConfig();
        getCommand("giftcode").setExecutor(new GiftCodeCommand());
        getCommand("code").setExecutor(new RedeemCodeCommand());
    }

    private void createConfig() {
        configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            saveResource("config.yml", false);
        }

        config = new YamlConfiguration();
        try {
            config.load(configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveConfigFile() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class GiftCodeCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (args.length < 1) {
                return false;
            }

            switch (args[0].toLowerCase()) {
                case "create":
                    if (args.length < 2) {
                        sender.sendMessage("Usage: /giftcode create <code>");
                        return true;
                    }
                    String code = args[1];
                    if (config.contains("codes." + code)) {
                        sender.sendMessage("Code already exists.");
                        return true;
                    }
                    config.set("codes." + code + ".commands", Arrays.asList("give %player% diamond 1"));
                    config.set("codes." + code + ".message", "You have received 1 diamond!");
                    config.set("codes." + code + ".max-uses", 10);
                    config.set("codes." + code + ".expiry", "2024-12-31T23:59:59");
                    config.set("codes." + code + ".enabled", true);
                    saveConfigFile();
                    sender.sendMessage("Code created successfully.");
                    break;
                case "del":
                    if (args.length < 2) {
                        sender.sendMessage("Usage: /giftcode del <code>");
                        return true;
                    }
                    String codeToDelete = args[1];
                    if (!config.contains("codes." + codeToDelete)) {
                        sender.sendMessage("Code does not exist.");
                        return true;
                    }
                    config.set("codes." + codeToDelete, null);
                    saveConfigFile();
                    sender.sendMessage("Code deleted successfully.");
                    break;
                case "reload":
                    createConfig();
                    sender.sendMessage("Config reloaded.");
                    break;
                case "enable":
                case "disable":
                    if (args.length < 2) {
                        sender.sendMessage("Usage: /giftcode <enable|disable> <code>");
                        return true;
                    }
                    String action = args[0].toLowerCase();
                    String codeToToggle = args[1];
                    if (!config.contains("codes." + codeToToggle)) {
                        sender.sendMessage("Code does not exist.");
                        return true;
                    }
                    boolean enable = action.equals("enable");
                    config.set("codes." + codeToToggle + ".enabled", enable);
                    saveConfigFile();
                    sender.sendMessage("Code " + codeToToggle + " " + (enable ? "enabled" : "disabled") + ".");
                    break;
                case "help":
                    sender.sendMessage("/giftcode create <code> - Create a new gift code");
                    sender.sendMessage("/giftcode del <code> - Delete an existing gift code");
                    sender.sendMessage("/giftcode reload - Reload the config file");
                    sender.sendMessage("/giftcode enable <code> - Enable a gift code");
                    sender.sendMessage("/giftcode disable <code> - Disable a gift code");
                    sender.sendMessage("/code <code> - Redeem a gift code");
                    sender.sendMessage("");
                    sender.sendMessage("Author: QuangDev05");
                    sender.sendMessage("Version: 1.0.0 | Stable");
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    public class RedeemCodeCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can redeem codes.");
                return true;
            }
            if (args.length < 1) {
                return false;
            }
            Player player = (Player) sender;
            String code = args[0];
            if (!config.contains("codes." + code)) {
                player.sendMessage(config.getString("messages.invalid-code"));
                return true;
            }

            boolean enabled = config.getBoolean("codes." + code + ".enabled");
            if (!enabled) {
                player.sendMessage(config.getString("messages.code-disabled"));
                return true;
            }

            LocalDateTime expiry = LocalDateTime.parse(config.getString("codes." + code + ".expiry"), DateTimeFormatter.ISO_DATE_TIME);
            if (LocalDateTime.now().isAfter(expiry)) {
                player.sendMessage(config.getString("messages.expired-code"));
                return true;
            }

            int maxUses = config.getInt("codes." + code + ".max-uses");
            int uses = config.getInt("codes." + code + ".uses", 0);
            if (uses >= maxUses) {
                player.sendMessage(config.getString("messages.max-uses-reached"));
                return true;
            }

            // Check if the player has already redeemed this code
            List<String> redeemedCodes = config.getStringList("players." + player.getUniqueId() + ".redeemed");
            if (redeemedCodes.contains(code)) {
                player.sendMessage(config.getString("messages.code-already-redeemed"));
                return true;
            }

            // Add the code to the list of redeemed codes for the player
            redeemedCodes.add(code);
            config.set("players." + player.getUniqueId() + ".redeemed", redeemedCodes);

            // Check the player's max uses
            int playerMaxUses = config.getInt("player-max-uses", 1);
            int playerUses = config.getInt("players." + player.getUniqueId() + ".uses", 0);
            if (playerUses >= playerMaxUses) {
                player.sendMessage("You have reached the maximum number of uses for gift codes.");
                return true;
            }

            List<String> commands = config.getStringList("codes." + code + ".commands");
            for (String cmd : commands) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
            }

            player.sendMessage(config.getString("codes." + code + ".message"));
            config.set("codes." + code + ".uses", uses + 1);
            config.set("players." + player.getUniqueId() + ".uses", playerUses + 1);
            saveConfigFile();
            player.sendMessage(config.getString("messages.code-redeemed"));

            return true;
        }
    }
}
