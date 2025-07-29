package quangdev05.giftcode24;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Statistic;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.Sound;
import org.bstats.bukkit.Metrics;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.*;

import org.json.JSONObject;

public class GiftCode24 extends JavaPlugin implements Listener {

    private FileConfiguration giftCodesConfig;
    private File giftCodesFile;
    private Map<String, GiftCode> giftCodes = new LinkedHashMap<>();
    private File dataplayerFile;
    private FileConfiguration dataplayerConfig;
    private String currentVersion = getDescription().getVersion();
    private String latestVersion = null;

    private static final int ITEMS_PER_PAGE = 45; // 5 rows
    private static final int PREV_BUTTON_SLOT = 45; // Last surrender cell
    private static final int NEXT_BUTTON_SLOT = 53; // Cell at the end of the last row
    private static final int PAGE_INFO_SLOT = 49;   // Middle of the last row

    private ItemStack createNavigationButton(String displayName, Material material, String lore) {
        ItemStack button = new ItemStack(material);
        ItemMeta meta = button.getItemMeta();

        meta.setDisplayName(ChatColor.GREEN + displayName);

        List<String> lores = new ArrayList<>();
        lores.add(ChatColor.GRAY + lore);
        meta.setLore(lores);

        button.setItemMeta(meta);
        return button;
    }

    private ItemStack createGiftCodeItem(String code, GiftCode giftCode) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.GOLD + code);

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Gift code information:");
        lore.add(ChatColor.WHITE + "• " + ChatColor.GRAY + "Quantity: " + ChatColor.YELLOW + giftCode.getMaxUses());

        int usedCount = calculateUsedCount(code);
        lore.add(ChatColor.WHITE + "• " + ChatColor.GRAY + "Used: " + ChatColor.YELLOW + usedCount);

        String expiry = giftCode.getExpiry().isEmpty() ?
                ChatColor.GREEN + "Indefinitely" :
                ChatColor.YELLOW + giftCode.getExpiry();
        lore.add(ChatColor.WHITE + "• " + ChatColor.GRAY + "Indefinite Expiry Date: " + expiry);

        String status = giftCode.isEnabled() ?
                ChatColor.GREEN + "Activating" :
                ChatColor.RED + "Disabled";
        lore.add(ChatColor.WHITE + "• " + ChatColor.GRAY + "Status: " + status);

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createPageInfoItem(int currentPage, int totalPages) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.GOLD + "Page " + (currentPage + 1) + "/" + totalPages);

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Use the buttons to turn pages");
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }

    @Override
    public void onEnable() {
        createConfigFiles();
        loadGiftCodes();
        loadDataplayerConfig();
        updateConfig();
        getCommand("giftcode").setExecutor(this);
        getCommand("code").setExecutor(this);
        getServer().getPluginManager().registerEvents(this, this);

        sendFancyMessage();
        checkForUpdates();

        int pluginId = 24198;
        Metrics metrics = new Metrics(this, pluginId);
    }

    private void createConfigFiles() {
        createFile("config.yml");
        createFile("giftcode.yml");
        createFile("dataplayer.yml");
    }

    private void createFile(String fileName) {
        File file = new File(getDataFolder(), fileName);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            saveResource(fileName, false);
        }
        if (fileName.equals("config.yml")) {
            getConfig().options().copyDefaults(true);
            saveConfig();
        } else if (fileName.equals("giftcode.yml")) {
            giftCodesFile = file;
            giftCodesConfig = YamlConfiguration.loadConfiguration(giftCodesFile);
        } else if (fileName.equals("dataplayer.yml")) {
            dataplayerFile = file;
            dataplayerConfig = YamlConfiguration.loadConfiguration(dataplayerFile);
        }
    }

    private void loadDataplayerConfig() {
        dataplayerConfig = YamlConfiguration.loadConfiguration(dataplayerFile);
    }

    private void sendFancyMessage() {
        getLogger().info(" ");
        getLogger().info(" ██████╗ ██╗███████╗████████╗ ██████╗ ██████╗ ██████╗ ███████╗██████╗ ██╗  ██╗");
        getLogger().info("██╔════╝ ██║██╔════╝╚══██╔══╝██╔════╝██╔═══██╗██╔══██╗██╔════╝╚════██╗██║  ██║");
        getLogger().info("██║  ███╗██║█████╗     ██║   ██║     ██║   ██║██║  ██║█████╗   █████╔╝███████║");
        getLogger().info("██║   ██║██║██╔══╝     ██║   ██║     ██║   ██║██║  ██║██╔══╝  ██╔═══╝ ╚════██║");
        getLogger().info("╚██████╔╝██║██║        ██║   ╚██████╗╚██████╔╝██████╔╝███████╗███████╗     ██║");
        getLogger().info(" ╚═════╝ ╚═╝╚═╝        ╚═╝    ╚═════╝ ╚═════╝ ╚═════╝ ╚══════╝╚══════╝     ╚═╝");
        getLogger().info(" ");
        getLogger().info("  Author: QuangDev05");
        getLogger().info("  Current version: v" + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        saveGiftCodes();
    }

    private void updateConfig() {
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    private void loadGiftCodes() {
        giftCodes.clear();
        for (String key : new ArrayList<>(giftCodesConfig.getKeys(false))) {
            Object messageObj = giftCodesConfig.get(key + ".message");
            List<String> commands = giftCodesConfig.getStringList(key + ".commands");
            int maxUses = giftCodesConfig.getInt(key + ".max-uses");
            String expiry = giftCodesConfig.getString(key + ".expiry");
            boolean enabled = giftCodesConfig.getBoolean(key + ".enabled");
            int playerMaxUses = giftCodesConfig.getInt(key + ".player-max-uses");
            int maxUsesPerIP = giftCodesConfig.getInt(key + ".player-max-uses-perip");
            int requiredPlaytime = giftCodesConfig.getInt(key + ".required-playtime");
            GiftCode giftCode = new GiftCode(commands, messageObj, maxUses, expiry, enabled, playerMaxUses, maxUsesPerIP, requiredPlaytime);
            if (giftCodesConfig.isConfigurationSection(key + ".ip-usage-counts")) {
                ConfigurationSection section = giftCodesConfig.getConfigurationSection(key + ".ip-usage-counts");
                for (String ip : section.getKeys(false)) {
                    int usage = section.getInt(ip);
                    giftCode.ipUsageCounts.put(ip, usage);
                }
            }
            giftCodes.put(key, giftCode);
        }
    }

    private void saveGiftCodes() {
        for (Map.Entry<String, GiftCode> entry : giftCodes.entrySet()) {
            GiftCode giftCode = entry.getValue();
            giftCodesConfig.set(entry.getKey() + ".commands", giftCode.getCommands());
            giftCodesConfig.set(entry.getKey() + ".message", giftCode.getMessages());
            giftCodesConfig.set(entry.getKey() + ".max-uses", giftCode.getMaxUses());
            giftCodesConfig.set(entry.getKey() + ".expiry", giftCode.getExpiry());
            giftCodesConfig.set(entry.getKey() + ".enabled", giftCode.isEnabled());
            giftCodesConfig.set(entry.getKey() + ".player-max-uses", giftCode.getPlayerMaxUses());
            giftCodesConfig.set(entry.getKey() + ".player-max-uses-perip", giftCode.getMaxUsesPerIP());
            giftCodesConfig.set(entry.getKey() + ".required-playtime", giftCode.getRequiredPlaytime());
            String ipUsageCountsPath = entry.getKey() + ".ip-usage-counts";
            giftCodesConfig.set(ipUsageCountsPath, null);
            for (Map.Entry<String, Integer> ipEntry : giftCode.ipUsageCounts.entrySet()) {
                giftCodesConfig.set(ipUsageCountsPath + "." + ipEntry.getKey(), ipEntry.getValue());
            }
        }
        try {
            giftCodesConfig.save(giftCodesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createGiftCode(String code, List<String> commands, List<String> message, int maxUses, String expiry,
                                boolean enabled, int playerMaxUses, int maxUsesPerIP, int requiredPlaytime) {
        if (giftCodes.containsKey(code)) {
            getLogger().warning("Gift code \"" + code + "\" already exists. Please create another code.");
            return;
        }

        GiftCode giftCode = new GiftCode(commands, message, maxUses, expiry, enabled, playerMaxUses, maxUsesPerIP, requiredPlaytime);
        giftCodes.put(code, giftCode);
        giftCodesConfig.set(code + ".commands", commands);
        giftCodesConfig.set(code + ".message", message);
        giftCodesConfig.set(code + ".max-uses", maxUses);
        giftCodesConfig.set(code + ".expiry", expiry);
        giftCodesConfig.set(code + ".enabled", enabled);
        giftCodesConfig.set(code + ".player-max-uses", playerMaxUses);
        giftCodesConfig.set(code + ".player-max-uses-perip", maxUsesPerIP);
        giftCodesConfig.set(code + ".required-playtime", requiredPlaytime);
        saveGiftCodes();
        getLogger().info("Gift code \"" + code + "\" has been successfully generated!");
    }

    private void deleteGiftCode(String code) {
        giftCodes.remove(code);
        giftCodesConfig.set(code, null);
        saveGiftCodes();
    }

    private List<String> listGiftCodes() {
        return new ArrayList<>(giftCodes.keySet());
    }

    private void assignGiftCodeToPlayer(CommandSender sender, String code, Player player) {
        if (!giftCodes.containsKey(code)) {
            sender.sendMessage(ChatColor.RED + "Gift code " + ChatColor.YELLOW + "\"" + code + "\"" + ChatColor.RED + " does not exist!");
            getLogger().warning("Gift code \"" + code + "\" does not exist!");
            return;
        }
        if (giftCodes.containsKey(code)) {
            GiftCode giftCode = giftCodes.get(code);
            List<String> assignedCodes = dataplayerConfig
                    .getStringList("players." + player.getUniqueId() + ".assignedCodes");
            if (assignedCodes == null) {
                assignedCodes = new ArrayList<>();
            }
            assignedCodes.add(code);
            dataplayerConfig.set("players." + player.getUniqueId() + ".assignedCodes", assignedCodes);
            saveDataplayerConfig();

            for (String cmd : giftCode.getCommands()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
            }
            player.sendMessage(ChatColor.GREEN + "You have been assigned the gift code " + ChatColor.YELLOW + "\"" + code + "\"" + ChatColor.GREEN + "!");
            for (String msg : giftCode.getMessages()) {
                player.sendMessage(ChatColor.GREEN + msg);
            }
            sender.sendMessage(ChatColor.GREEN + "Assigned gift code \"" + ChatColor.YELLOW + code + ChatColor.GREEN + "\" to player \"" + ChatColor.AQUA + player.getName() + ChatColor.GREEN + "\".");
            getLogger().info("Gift code \"" + code + "\" has been successfully assigned to the player \"" + player.getName() + "\"");
        }
    }

    private void createRandomGiftCodes(String baseName, int amount) {
        for (int i = 0; i < amount; i++) {
            String code = baseName + "_" + ThreadLocalRandom.current().nextInt(1_000_000);
            createGiftCode(code, Collections.singletonList("give %player% diamond 1"),
                    Collections.singletonList("You have received a diamond!"), 99, "2029-12-31T23:59:59", true, 1, 1, 8);
        }
    }

    private boolean checkPlayerHasUsedCode(Player player, String code) {
        List<String> usedCodes = dataplayerConfig.getStringList("players." + player.getUniqueId() + ".usedCodes");
        int playerMaxUses = getPlayerMaxUsesForCode(code);
        if (playerMaxUses == -1) {
            return false;
        }
        return Collections.frequency(usedCodes, code) >= playerMaxUses;
    }

    private void addPlayerUsedCode(Player player, String code) {
        List<String> usedCodes = dataplayerConfig.getStringList("players." + player.getUniqueId() + ".usedCodes");
        String playerIP = player.getAddress().getAddress().getHostAddress();
        if (usedCodes == null) {
            usedCodes = new ArrayList<>();
        }
        usedCodes.add(code);
        dataplayerConfig.set("players." + player.getUniqueId() + ".ip", playerIP);

        dataplayerConfig.set("players." + player.getUniqueId() + ".usedCodes", usedCodes);
        saveDataplayerConfig();
    }

    private void saveDataplayerConfig() {
        try {
            dataplayerConfig.save(dataplayerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openGiftCodeList(Player player, int page) {
        List<String> codeList = new ArrayList<>(giftCodes.keySet());
        int totalCodes = codeList.size();
        int totalPages = (int) Math.ceil((double) totalCodes / ITEMS_PER_PAGE);

        page = Math.max(0, Math.min(page, totalPages - 1));

        Inventory inv = Bukkit.createInventory(
                null,
                54,
                ChatColor.GOLD + "List of gift codes"
        );

        int start = page * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, totalCodes);

        for (int i = start; i < end; i++) {
            String code = codeList.get(i);
            GiftCode giftCode = giftCodes.get(code);
            ItemStack item = createGiftCodeItem(code, giftCode);
            inv.addItem(item);
        }

        if (totalPages > 1) {
            if (page > 0) {
                ItemStack prevButton = createNavigationButton(
                        "« Previous Page",
                        Material.ARROW,
                        "Click to return to page " + page
                );
                inv.setItem(PREV_BUTTON_SLOT, prevButton);
            }

            ItemStack pageInfo = createPageInfoItem(page, totalPages);
            inv.setItem(PAGE_INFO_SLOT, pageInfo);

            if (page < totalPages - 1) {
                ItemStack nextButton = createNavigationButton(
                        "Next Page »",
                        Material.ARROW,
                        "Click to page" + (page + 2)
                );
                inv.setItem(NEXT_BUTTON_SLOT, nextButton);
            }
        }

        player.openInventory(inv);
    }

    private int calculateUsedCount(String code) {
        int count = 0;
        if (dataplayerConfig.getConfigurationSection("players") != null) {
            for (String uuid : dataplayerConfig.getConfigurationSection("players").getKeys(false)) {
                List<String> usedCodes = dataplayerConfig.getStringList("players." + uuid + ".usedCodes");
                count += Collections.frequency(usedCodes, code);
            }
        }
        return count;
    }

    private void checkForUpdates() {
        boolean checkUpdate = getConfig().getBoolean("check-update", true);

        if (!checkUpdate) {
            return;
        }

        String url = "https://api.github.com/repos/quangdev05/GiftCode24/releases/latest";
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        Scanner scanner = new Scanner(connection.getInputStream());
                        String response = scanner.useDelimiter("\\A").next();
                        scanner.close();

                        JSONObject jsonResponse = new JSONObject(response);
                        String fetchedVersion = jsonResponse.getString("tag_name");

                        latestVersion = fetchedVersion;

                        if (!latestVersion.equals(currentVersion)) {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    getLogger().info("Plugin has new version: v" + latestVersion);
                                }
                            }.runTaskTimer(GiftCode24.this, 0L, 6000L);
                        } else {
                            getLogger().info(
                                    "Plugin is at latest version v" + currentVersion);
                        }
                    } else {
                        getLogger().warning("Unable to connect to check for updates.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskLater(this, 0L);
    }

    private int getPlayerMaxUsesForCode(String code) {
        if (giftCodes.containsKey(code)) {
            return giftCodes.get(code).getPlayerMaxUses();
        }
        return 1;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("giftcode") || label.equalsIgnoreCase("gc")) {
            if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
                sender.sendMessage(ChatColor.GOLD + "Command List");
                sender.sendMessage(ChatColor.GREEN + " /gc create <code> - Create a gift code");
                sender.sendMessage(ChatColor.GREEN + " /gc create <name> random - Generate 10 random gift codes");
                sender.sendMessage(ChatColor.GREEN + " /gc del <code> - Delete a gift code");
                sender.sendMessage(ChatColor.GREEN + " /gc reload - Reload the plugin");
                sender.sendMessage(ChatColor.GREEN + " /gc enable <code> - Enable a gift code");
                sender.sendMessage(ChatColor.GREEN + " /gc disable <code> - Disable a gift code");
                sender.sendMessage(ChatColor.GREEN + " /gc list - List all gift codes");
                sender.sendMessage(ChatColor.GREEN + " /gc assign <code> <player> - Assign a gift code to a player");
                sender.sendMessage("");
                sender.sendMessage(ChatColor.GOLD + "Information:");
                sender.sendMessage(ChatColor.YELLOW + " Author: QuangDev05");
                sender.sendMessage(ChatColor.YELLOW + " Current version: v" + currentVersion);
                sender.sendMessage(ChatColor.YELLOW + " Latest version: " +
                        (latestVersion != null ? "v" + latestVersion : "Unknown"));
                return true;
            }

            if (!sender.hasPermission("giftcode.admin")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "create":
                    if (args.length == 2) {
                        if (giftCodes.containsKey(args[1])) {
                            sender.sendMessage(
                                    ChatColor.RED + "The gift code \"" + ChatColor.YELLOW + args[1] + ChatColor.RED + "\" already exists. Please create a different code.");
                        } else {
                            createGiftCode(args[1], Collections.singletonList("give %player% diamond 1"),
                                    Collections.singletonList("You have received 1 diamond!"), 99, "2029-12-31T23:59:59", true, 1, 1, 8);
                            sender.sendMessage(ChatColor.GREEN + "The gift code \"" + ChatColor.YELLOW + args[1] + ChatColor.GREEN + "\" has been created successfully!");
                        }
                    } else if (args.length == 3 && args[2].equalsIgnoreCase("random")) {
                        createRandomGiftCodes(args[1], 10);
                        sender.sendMessage(ChatColor.GREEN + "10 random gift codes have been generated with base name \"" + ChatColor.YELLOW + args[1] + "\"");
                    } else {
                        sender.sendMessage(
                                ChatColor.RED + "Usage: /giftcode create <code> or /giftcode create <name> random");
                    }
                    break;
                case "del":
                    if (args.length == 2) {
                        deleteGiftCode(args[1]);
                        sender.sendMessage(ChatColor.GREEN + "Gift code \"" + ChatColor.YELLOW + args[1] + ChatColor.GREEN + "\" has been deleted!");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Usage: /giftcode del <code>");
                    }
                    break;

                case "reload":
                    reloadConfig();
                    createConfigFiles();
                    loadGiftCodes();
                    loadDataplayerConfig();
                    sender.sendMessage(ChatColor.GREEN + "All configuration files have been reloaded!");
                    break;

                case "enable":
                    if (args.length == 2) {
                        GiftCode codeToEnable = giftCodes.get(args[1]);
                        if (codeToEnable != null) {
                            codeToEnable.setEnabled(true);
                            saveGiftCodes();
                            sender.sendMessage(ChatColor.GREEN + "Gift code \"" + ChatColor.YELLOW + args[1] + ChatColor.GREEN + "\" has been enabled!");
                        } else {
                            sender.sendMessage(ChatColor.RED + "Gift code does not exist!");
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
                            sender.sendMessage(ChatColor.GREEN + "Gift code \"" + ChatColor.YELLOW + args[1] + ChatColor.GREEN + "\" has been disabled!");
                        } else {
                            sender.sendMessage(ChatColor.RED + "Gift code does not exist!");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "Usage: /giftcode disable <code>");
                    }
                    break;

                case "list":
                    if (sender instanceof Player) {
                        openGiftCodeList((Player) sender, 0);
                    } else {
                        sender.sendMessage(ChatColor.GOLD + "Gift code list:");
                        for (String code : listGiftCodes()) {
                            sender.sendMessage(ChatColor.GREEN + " " + code);
                        }
                    }
                    break;

                case "assign":
                    if (args.length == 3) {
                        String code = args[1];
                        Player targetPlayer = Bukkit.getPlayer(args[2]);
                        if (targetPlayer != null) {
                            if (!giftCodes.containsKey(code)) {
                                sender.sendMessage(ChatColor.RED + "Gift code \"" + ChatColor.YELLOW + code + ChatColor.RED + "\" does not exist!");
                                return true;
                            }
                            assignGiftCodeToPlayer(sender, code, targetPlayer);
                        } else {
                            sender.sendMessage(ChatColor.RED + "Player not found: " + args[2]);
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "Usage: /giftcode assign <code> <player>");
                    }
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

                    if (giftCode.getRequiredPlaytime() > 0) {
                        int playerPlaytime = player.getStatistic(Statistic.PLAY_ONE_MINUTE) / (20 * 60);
                        if (playerPlaytime < giftCode.getRequiredPlaytime()) {
                            String message = getConfig().getString("messages.not-enough-playtime")
                                    .replace("{required}", String.valueOf(giftCode.getRequiredPlaytime()))
                                    .replace("{current}", String.valueOf(playerPlaytime));
                            player.sendMessage(ChatColor.RED + message);
                            return true;
                        }
                    }

                    if (!giftCode.getExpiry().isEmpty()) {
                        try {
                            Date expiryDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(giftCode.getExpiry());
                            if (new Date().after(expiryDate)) {
                                player.sendMessage(ChatColor.RED + getConfig().getString("messages.code-expired"));
                                return true;
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                            player.sendMessage(ChatColor.RED + "An error occurred while checking the expiration time.");
                            return true;
                        }
                    }

                    if (giftCode.getMaxUses() <= 0) {
                        player.sendMessage(ChatColor.RED + getConfig().getString("messages.max-uses-reached"));
                        return true;
                    }

                    if (giftCode.getMaxUsesPerIP() > 0) {
                        String playerIP = player.getAddress().getAddress().getHostAddress();
                        List<String> usedCodesByIP = new ArrayList<>();

                        ConfigurationSection playersSection = dataplayerConfig.getConfigurationSection("players");
                        if (playersSection != null) {
                            for (String uuid : playersSection.getKeys(false)) {
                                String ip = dataplayerConfig.getString("players." + uuid + ".ip");
                                if (playerIP.equals(ip)) {
                                    usedCodesByIP.addAll(dataplayerConfig.getStringList("players." + uuid + ".usedCodes"));
                                }
                            }
                        }

                        int ipUsageCount = Collections.frequency(usedCodesByIP, code);
                        if (ipUsageCount >= giftCode.getMaxUsesPerIP()) {
                            player.sendMessage(ChatColor.RED + getConfig().getString("messages.max-uses-perip"));
                            return true;
                        }
                    }

                    if (checkPlayerHasUsedCode(player, code)) {
                        player.sendMessage(ChatColor.RED + getConfig().getString("messages.code-already-redeemed"));
                        return true;
                    }

                    for (String cmd : giftCode.getCommands()) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
                    }

                    for (String msg : giftCode.getMessages()) {
                        player.sendMessage(ChatColor.GREEN + msg);
                    }

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

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();

        if (title.equals(ChatColor.GOLD + "List of gift codes")) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null) return;
            if (!(event.getWhoClicked() instanceof Player)) return;

            Player player = (Player) event.getWhoClicked();
            ItemStack clicked = event.getCurrentItem();

            int currentPage = 0;
            ItemStack pageInfo = event.getInventory().getItem(PAGE_INFO_SLOT);
            if (pageInfo != null && pageInfo.hasItemMeta()) {
                String displayName = pageInfo.getItemMeta().getDisplayName();
                if (displayName.contains("/")) {
                    try {
                        String[] parts = displayName.replace(ChatColor.GOLD + "Page ", "").split("/");
                        currentPage = Integer.parseInt(parts[0].trim()) - 1;
                    } catch (NumberFormatException ignored) {}
                }
            }

            if (clicked.getType() == Material.ARROW) {
                int totalGiftCodes = giftCodes.size();
                int totalPages = (int) Math.ceil((double) totalGiftCodes / ITEMS_PER_PAGE);
                if (event.getSlot() == PREV_BUTTON_SLOT && currentPage > 0) {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                    openGiftCodeList(player, currentPage - 1);
                }
                else if (event.getSlot() == NEXT_BUTTON_SLOT && currentPage < totalPages - 1) {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                    openGiftCodeList(player, currentPage + 1);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTitle().equals(ChatColor.GOLD + "List of gift codes")) {
            event.setCancelled(true);
        }
    }

    public class GiftCode {
        public Map<String, Integer> ipUsageCounts = new HashMap<>();

        private int maxUsesPerIP;
        private List<String> commands;
        private List<String> messages;
        private int maxUses;
        private String expiry;
        private boolean enabled;
        private int playerMaxUses;
        private int requiredPlaytime;

        public GiftCode(List<String> commands, Object messageObj, int maxUses, String expiry, boolean enabled,
                        int playerMaxUses, int maxUsesPerIP, int requiredPlaytime) {
            this.commands = commands;
            this.messages = new ArrayList<>();
            if (messageObj instanceof String) {
                this.messages.add((String) messageObj);
            } else if (messageObj instanceof List) {
                this.messages.addAll((List<String>) messageObj);
            }
            this.maxUses = maxUses;
            this.expiry = expiry;
            this.enabled = enabled;
            this.playerMaxUses = playerMaxUses;
            this.maxUsesPerIP = maxUsesPerIP;
            this.ipUsageCounts = new HashMap<>();
            this.requiredPlaytime = requiredPlaytime;
        }

        public List<String> getCommands() {
            return commands;
        }

        public void setCommands(List<String> commands) {
            this.commands = commands;
        }

        public List<String> getMessages() {
            return messages;
        }

        public void setMessage(List<String> messages) {
            this.messages = messages;
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

        public int getMaxUsesPerIP() {
            return maxUsesPerIP;
        }

        public void setMaxUsesPerIP(int maxUsesPerIP) {
            this.maxUsesPerIP = maxUsesPerIP;
        }

        public int getRequiredPlaytime() {
            return requiredPlaytime;}
    }
}