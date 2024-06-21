package quangdev05.giftcode24;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GiftCode24 extends JavaPlugin {

    private FileConfiguration giftCodesConfig;
    private File giftCodesFile;
    private Map<String, GiftCode> giftCodes = new HashMap<>();

    @Override
    public void onEnable() {
        createGiftCodesFile();
        loadGiftCodes();
        updateConfig(); // Call updateConfig() method to ensure default config is updated if necessary
        getCommand("giftcode").setExecutor(this);
        getCommand("code").setExecutor(this);

        if (getConfig().getBoolean("update-checker.enabled", true)) {
            new UpdateChecker(this).checkForUpdates();
        }

        // Send a fancy message to console
        sendFancyMessage();
    }

    private void sendFancyMessage() {
        getLogger().info(" ");
        getLogger().info("  ██████╗ ██████╗ ███╗   ██╗███████╗███████╗███████╗");
        getLogger().info("  ██╔══██╗██╔══██╗████╗  ██║██╔════╝██╔════╝██╔════╝");
        getLogger().info("  ██████╔╝██████╔╝██╔██╗ ██║███████╗█████╗  ███████╗");
        getLogger().info("  ██╔═══╝ ██╔══██╗██║╚██╗██║╚════██║██╔══╝  ╚════██║");
        getLogger().info("  ██║     ██║  ██║██║ ╚████║███████║███████╗███████║");
        getLogger().info("  ╚═╝     ╚═╝  ╚═╝╚═╝  ╚═══╝╚══════╝╚══════╝╚══════╝");
        getLogger().info(" ");
        getLogger().info("  Plugin by: QuangDev05");
        getLogger().info("  Version: " + getDescription().getVersion());
        getLogger().info(" ");
    }


    @Override
    public void onDisable() {
        saveGiftCodes();
    }

    private void updateConfig() {
        // Update config only if it doesn't exist or if it's outdated
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    private void createGiftCodesFile() {
        giftCodesFile = new File(getDataFolder(), "giftcode.yml");
        if (!giftCodesFile.exists()) {
            giftCodesFile.getParentFile().mkdirs();
            saveResource("giftcode.yml", false);
        }
        giftCodesConfig = YamlConfiguration.loadConfiguration(giftCodesFile);
    }

    private void loadGiftCodes() {
        for (String key : giftCodesConfig.getKeys(false)) {
            String message = giftCodesConfig.getString(key + ".message");
            List<String> commands = giftCodesConfig.getStringList(key + ".commands");
            int maxUses = giftCodesConfig.getInt(key + ".max-uses");
            String expiry = giftCodesConfig.getString(key + ".expiry");
            boolean enabled = giftCodesConfig.getBoolean(key + ".enabled");
            int playerMaxUses = giftCodesConfig.getInt(key + ".player-max-uses");
            GiftCode giftCode = new GiftCode(commands, message, maxUses, expiry, enabled, playerMaxUses);
            giftCodes.put(key, giftCode);
        }
    }

    private void saveGiftCodes() {
        for (Map.Entry<String, GiftCode> entry : giftCodes.entrySet()) {
            GiftCode giftCode = entry.getValue();
            giftCodesConfig.set(entry.getKey() + ".commands", giftCode.getCommands());
            giftCodesConfig.set(entry.getKey() + ".message", giftCode.getMessage());
            giftCodesConfig.set(entry.getKey() + ".max-uses", giftCode.getMaxUses());
            giftCodesConfig.set(entry.getKey() + ".expiry", giftCode.getExpiry());
            giftCodesConfig.set(entry.getKey() + ".enabled", giftCode.isEnabled());
            giftCodesConfig.set(entry.getKey() + ".player-max-uses", giftCode.getPlayerMaxUses());
        }
        try {
            giftCodesConfig.save(giftCodesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createGiftCode(String code, List<String> commands, String message, int maxUses, String expiry, boolean enabled, int playerMaxUses) {
        GiftCode giftCode = new GiftCode(commands, message, maxUses, expiry, enabled, playerMaxUses);
        giftCodes.put(code, giftCode);
        giftCodesConfig.set(code + ".commands", commands);
        giftCodesConfig.set(code + ".message", message);
        giftCodesConfig.set(code + ".max-uses", maxUses);
        giftCodesConfig.set(code + ".expiry", expiry);
        giftCodesConfig.set(code + ".enabled", enabled);
        giftCodesConfig.set(code + ".player-max-uses", playerMaxUses);
        saveGiftCodes();
    }

    private void deleteGiftCode(String code) {
        giftCodes.remove(code);
        giftCodesConfig.set(code, null);
        saveGiftCodes();
    }

    private List<String> listGiftCodes() {
        return new ArrayList<>(giftCodes.keySet());
    }

    private void assignGiftCodeToPlayer(String code, Player player) {
        if (giftCodes.containsKey(code)) {
            player.sendMessage(ChatColor.GREEN + "You have been assigned the gift code: " + code);
        } else {
            player.sendMessage(ChatColor.RED + "Gift code not found!");
        }
    }

    private void createRandomGiftCodes(String baseName, int amount) {
        Random random = new Random();
        for (int i = 0; i < amount; i++) {
            String code = baseName + "_" + random.nextInt(1000000);
            createGiftCode(code, Collections.singletonList("give %player% diamond 1"), "You have received 1 diamond!", 10, "2024-12-31T23:59:59", true, 1);
        }
    }

    private boolean checkPlayerHasUsedCode(Player player, String code) {
        return getConfig().getStringList("players." + player.getUniqueId() + ".usedCodes").contains(code);
    }

    private void addPlayerUsedCode(Player player, String code) {
        List<String> usedCodes = getConfig().getStringList("players." + player.getUniqueId() + ".usedCodes");
        usedCodes.add(code);
        getConfig().set("players." + player.getUniqueId() + ".usedCodes", usedCodes);
        saveConfig();
    }

    private int getPlayerMaxUsesForCode(String code) {
        return giftCodes.containsKey(code) ? giftCodes.get(code).getPlayerMaxUses() : 1;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("giftcode")) {
            if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
                sender.sendMessage(ChatColor.YELLOW + "GiftCode Plugin Commands:");
                sender.sendMessage(ChatColor.YELLOW + "/giftcode create <code> - Create a new gift code");
                sender.sendMessage(ChatColor.YELLOW + "/giftcode create <name> random - Create a batch of 10 random gift codes");
                sender.sendMessage(ChatColor.YELLOW + "/giftcode del <code> - Delete a gift code");
                sender.sendMessage(ChatColor.YELLOW + "/giftcode reload - Reload the configuration file");
                sender.sendMessage(ChatColor.YELLOW + "/giftcode enable <code> - Enable a gift code");
                sender.sendMessage(ChatColor.YELLOW + "/giftcode disable <code> - Disable a gift code");
                sender.sendMessage(ChatColor.YELLOW + "/giftcode list - List all created gift codes");
                sender.sendMessage(ChatColor.YELLOW + "/giftcode <code> <player> - Assign a gift code to a specified player");
                sender.sendMessage(ChatColor.YELLOW + "Author: QuangDev05");
                sender.sendMessage(ChatColor.YELLOW + "Version: 1.2.0 | Stable");
                return true;
            }

            if (!sender.hasPermission("giftcode.admin")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "create":
                    if (args.length == 2) {
                        createGiftCode(args[1], Collections.singletonList("give %player% diamond 1"), "You have received 1 diamond!", 10, "2024-12-31T23:59:59", true, 1);
                        sender.sendMessage(ChatColor.GREEN + "Gift code " + args[1] + " created successfully!");
                    } else if (args.length == 3 && args[2].equalsIgnoreCase("random")) {
                        createRandomGiftCodes(args[1], 10);
                        sender.sendMessage(ChatColor.GREEN + "Created 10 random gift codes with base name " + args[1]);
                    } else {
                        sender.sendMessage(ChatColor.RED + "Usage: /giftcode create <code> or /giftcode create <name> random");
                    }
                    break;
                case "del":
                    if (args.length == 2) {
                        deleteGiftCode(args[1]);
                        sender.sendMessage(ChatColor.GREEN + "Gift code " + args[1] + " deleted successfully!");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Usage: /giftcode del <code>");
                    }
                    break;
                case "reload":
                    reloadConfig();
                    createGiftCodesFile();
                    loadGiftCodes();
                    sender.sendMessage(ChatColor.GREEN + "Configuration reloaded successfully!");
                    break;
                case "enable":
                    if (args.length == 2) {
                        GiftCode codeToEnable = giftCodes.get(args[1]);
                        if (codeToEnable != null) {
                            codeToEnable.setEnabled(true);
                            saveGiftCodes();
                            sender.sendMessage(ChatColor.GREEN + "Gift code " + args[1] + " enabled successfully!");
                        } else {
                            sender.sendMessage(ChatColor.RED + "Gift code not found!");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "Usage: /giftcode enable <code>");
                    }
                    break;
                case "disable":
                    if (args.length == 2) {
                        GiftCode codeToDisable = giftCodes.get(args[1]);
                        if (codeToDisable != null) {
                            codeToDisable.setEnabled(false);
                            saveGiftCodes();
                            sender.sendMessage(ChatColor.GREEN + "Gift code " + args[1] + " disabled successfully!");
                        } else {
                            sender.sendMessage(ChatColor.RED + "Gift code not found!");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "Usage: /giftcode disable <code>");
                    }
                    break;
                case "list":
                    sender.sendMessage(ChatColor.YELLOW + "List of Gift Codes:");
                    for (String code : listGiftCodes()) {
                        sender.sendMessage(ChatColor.YELLOW + code);
                    }
                    break;
                case "assign":
                    if (args.length == 3) {
                        Player targetPlayer = Bukkit.getPlayer(args[2]);
                        if (targetPlayer != null) {
                            assignGiftCodeToPlayer(args[1], targetPlayer);
                        } else {
                            sender.sendMessage(ChatColor.RED + "Player not found!");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "Usage: /giftcode <code> <player>");
                    }
                    break;
                default:
                    sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /giftcode help for a list of commands.");
                    break;
            }
            return true;
        } else if (label.equalsIgnoreCase("code")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (args.length == 1) {
                    String code = args[0];
                    GiftCode giftCode = giftCodes.get(code);
                    if (giftCode == null) {
                        player.sendMessage(ChatColor.RED + getConfig().getString("messages.invalid-code"));
                        return true;
                    }
                    if (!giftCode.isEnabled()) {
                        player.sendMessage(ChatColor.RED + getConfig().getString("messages.code-disabled"));
                        return true;
                    }
                    if (giftCode.getMaxUses() <= 0) {
                        player.sendMessage(ChatColor.RED + getConfig().getString("messages.max-uses-reached"));
                        return true;
                    }
                    if (checkPlayerHasUsedCode(player, code)) {
                        player.sendMessage(ChatColor.RED + getConfig().getString("messages.code-already-redeemed"));
                        return true;
                    }
                    int playerMaxUses = getPlayerMaxUsesForCode(code);
                    if (Collections.frequency(getConfig().getStringList("players." + player.getUniqueId() + ".usedCodes"), code) >= playerMaxUses) {
                        player.sendMessage(ChatColor.RED + getConfig().getString("messages.code-already-redeemed"));
                        return true;
                    }
                    // Execute commands
                    for (String cmd : giftCode.getCommands()) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
                    }
                    player.sendMessage(ChatColor.GREEN + giftCode.getMessage());
                    giftCode.setMaxUses(giftCode.getMaxUses() - 1);
                    addPlayerUsedCode(player, code);
                    saveGiftCodes();
                } else {
                    player.sendMessage(ChatColor.RED + "Usage: /code <code>");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            }
            return true;
        }
        return false;
    }

    public class GiftCode {
        private List<String> commands;
        private String message;
        private int maxUses;
        private String expiry;
        private boolean enabled;
        private int playerMaxUses;

        public GiftCode(List<String> commands, String message, int maxUses, String expiry, boolean enabled, int playerMaxUses) {
            this.commands = commands;
            this.message = message;
            this.maxUses = maxUses;
            this.expiry = expiry;
            this.enabled = enabled;
            this.playerMaxUses = playerMaxUses;
        }

        public List<String> getCommands() {
            return commands;
        }

        public void setCommands(List<String> commands) {
            this.commands = commands;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getMaxUses() {
            return maxUses;
        }

        public void setMaxUses(int maxUses) {
            this.maxUses = maxUses;
        }

        public String getExpiry() {
            return expiry;
        }

        public void setExpiry(String expiry) {
            this.expiry = expiry;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getPlayerMaxUses() {
            return playerMaxUses;
        }

        public void setPlayerMaxUses(int playerMaxUses) {
            this.playerMaxUses = playerMaxUses;
        }
    }
}
