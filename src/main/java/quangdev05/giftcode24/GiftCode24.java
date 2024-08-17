package quangdev05.giftcode24;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.*;

import org.json.JSONObject;

public class GiftCode24 extends JavaPlugin {

    private FileConfiguration giftCodesConfig;
    private File giftCodesFile;
    private Map<String, GiftCode> giftCodes = new HashMap<>();
    private File dataplayerFile;
    private FileConfiguration dataplayerConfig;
    private String currentVersion = getDescription().getVersion();

    @Override
    public void onEnable() {
        createConfigFiles(); // Tạo tất cả các file cấu hình
        loadGiftCodes();
        loadDataplayerConfig();
        updateConfig(); // Cập nhật config mặc định
        getCommand("giftcode").setExecutor(this);
        getCommand("code").setExecutor(this);

        // Gửi thông điệp đẹp mắt đến console
        sendFancyMessage();
        // Kiểm tra cập nhật sau khi gửi FancyMessage
        checkForUpdates();
    }

    private void createConfigFiles() {
        createFile("config.yml"); // Thêm dòng này
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
            // Đảm bảo nạp lại config
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
        getLogger().info(ChatColor.GREEN + " ██████╗ ██╗███████╗████████╗ ██████╗ ██████╗ ██████╗ ███████╗██████╗ ██╗  ██╗");
        getLogger().info(ChatColor.GREEN + "██╔════╝ ██║██╔════╝╚══██╔══╝██╔════╝██╔═══██╗██╔══██╗██╔════╝╚════██╗██║  ██║");
        getLogger().info(ChatColor.GREEN + "██║  ███╗██║█████╗     ██║   ██║     ██║   ██║██║  ██║█████╗   █████╔╝███████║");
        getLogger().info(ChatColor.GREEN + "██║   ██║██║██╔══╝     ██║   ██║     ██║   ██║██║  ██║██╔══╝  ██╔═══╝ ╚════██║");
        getLogger().info(ChatColor.GREEN + "╚██████╔╝██║██║        ██║   ╚██████╗╚██████╔╝██████╔╝███████╗███████╗     ██║");
        getLogger().info(ChatColor.GREEN + " ╚═════╝ ╚═╝╚═╝        ╚═╝    ╚═════╝ ╚═════╝ ╚═════╝ ╚══════╝╚══════╝     ╚═╝");
        getLogger().info(" ");
        getLogger().info(ChatColor.GOLD + "  Tác giả: QuangDev05");
        getLogger().info(ChatColor.YELLOW + "  Phiên bản: v" + getDescription().getVersion());
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

    private void createGiftCode(String code, List<String> commands, String message, int maxUses, String expiry,
            boolean enabled, int playerMaxUses) {
        if (giftCodes.containsKey(code)) {
            getLogger().warning("Mã quà tặng " + code + " đã tồn tại. Vui lòng tạo mã khác.");
            return; // Dừng lại nếu mã đã tồn tại
        }

        GiftCode giftCode = new GiftCode(commands, message, maxUses, expiry, enabled, playerMaxUses);
        giftCodes.put(code, giftCode);
        giftCodesConfig.set(code + ".commands", commands);
        giftCodesConfig.set(code + ".message", message);
        giftCodesConfig.set(code + ".max-uses", maxUses);
        giftCodesConfig.set(code + ".expiry", expiry);
        giftCodesConfig.set(code + ".enabled", enabled);
        giftCodesConfig.set(code + ".player-max-uses", playerMaxUses);
        saveGiftCodes(); // Cập nhật lại file cấu hình
        getLogger().info("Mã quà tặng " + code + " đã được tạo thành công!"); // Thông báo chỉ sau khi mã đã được tạo
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
        if (giftCodes.containsKey(code)) {
            GiftCode giftCode = giftCodes.get(code);
            if (!giftCode.isEnabled()) {
                sender.sendMessage(ChatColor.RED + "Mã quà tặng " + code + " đã bị vô hiệu hóa.");
                return;
            }

            // Lưu mã quà tặng vào cấu hình của người chơi
            List<String> assignedCodes = dataplayerConfig
                    .getStringList("players." + player.getUniqueId() + ".assignedCodes");
            if (assignedCodes == null) {
                assignedCodes = new ArrayList<>();
            }
            if (!assignedCodes.contains(code)) {
                assignedCodes.add(code);
                dataplayerConfig.set("players." + player.getUniqueId() + ".assignedCodes", assignedCodes);
                saveDataplayerConfig();

                // Thực hiện lệnh tặng quà tặng cho người chơi
                for (String cmd : giftCode.getCommands()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
                }
                player.sendMessage(ChatColor.GREEN + "Bạn đã được gán mã quà tặng " + code + "!");
                player.sendMessage(ChatColor.GREEN + giftCode.getMessage());
            } else {
                sender.sendMessage(ChatColor.RED + "Người chơi đã được gán hoặc đã nhập mã quà tặng này rồi.");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Không tìm thấy mã quà tặng!");
        }
    }

    private void createRandomGiftCodes(String baseName, int amount) {
        Random random = new Random();
        for (int i = 0; i < amount; i++) {
            String code = baseName + "_" + random.nextInt(1000000);
            createGiftCode(code, Collections.singletonList("give %player% diamond 1"),
                    "Bạn đã nhận được một viên kim cương!", 10, "2024-12-31T23:59:59", true, 1);
        }
    }

    private boolean checkPlayerHasUsedCode(Player player, String code) {
        List<String> usedCodes = dataplayerConfig.getStringList("players." + player.getUniqueId() + ".usedCodes");
        int playerMaxUses = getPlayerMaxUsesForCode(code);
        if (playerMaxUses == -1) {
            return false; // Không giới hạn
        }
        return Collections.frequency(usedCodes, code) >= playerMaxUses;
    }

    private void addPlayerUsedCode(Player player, String code) {
        List<String> usedCodes = dataplayerConfig.getStringList("players." + player.getUniqueId() + ".usedCodes");
        if (usedCodes == null) {
            usedCodes = new ArrayList<>();
        }
        usedCodes.add(code);
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

    private void checkForUpdates() {
        boolean checkUpdate = getConfig().getBoolean("check-update", true);

        if (!checkUpdate) {
            return; // Không kiểm tra cập nhật nếu tùy chọn check-update là false
        }

        String url = "https://api.github.com/repos/QuangDev05/GiftCode24/releases/latest"; // Thay thế bằng URL của bạn
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

                        // Phân tích JSON để lấy phiên bản mới nhất
                        JSONObject jsonResponse = new JSONObject(response);
                        String latestVersion = jsonResponse.getString("tag_name");

                        // Loại bỏ chữ 'v' nếu có
                        if (latestVersion.startsWith("v")) {
                            latestVersion = latestVersion.substring(1);
                        }

                        // Biến final hoặc effectively final để sử dụng trong inner class
                        final String finalLatestVersion = latestVersion;

                        // Kiểm tra và thông báo nếu phiên bản mới hơn
                        if (!finalLatestVersion.equals(currentVersion)) {
                            getLogger().info(ChatColor.YELLOW + "Đã có phiên bản mới của plugin! Phiên bản hiện tại: v"
                                    + currentVersion + ", Phiên bản mới: v" + finalLatestVersion);
                            // Gửi thông báo cho admin mỗi 18 phút
                            Bukkit.getScheduler().runTaskTimer(GiftCode24.this, new BukkitRunnable() {
                                @Override
                                public void run() {
                                    getLogger().info(ChatColor.YELLOW
                                            + "Plugin đã có phiên bản mới: v" + finalLatestVersion);
                                }
                            }, 0L, 1080L); // 1080 ticks = 18 phút
                        } else {
                            getLogger().info(
                                    ChatColor.GREEN + "Plugin đang ở phiên bản mới nhất v" + currentVersion);
                        }
                    } else {
                        getLogger().warning("Không thể kết nối để kiểm tra bản cập nhật.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskLater(this, 0L); // Chạy ngay lập tức khi plugin khởi động
    }

    private int getPlayerMaxUsesForCode(String code) {
        if (giftCodes.containsKey(code)) {
            int playerMaxUses = giftCodes.get(code).getPlayerMaxUses();
            return playerMaxUses;
        }
        return 1; // Giá trị mặc định nếu không tìm thấy mã
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("giftcode") || label.equalsIgnoreCase("gc")) {
            if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
                sender.sendMessage(ChatColor.GOLD + "Danh sách lệnh:");
                sender.sendMessage(ChatColor.GREEN + "/giftcode create <code> - Tạo mã quà tặng");
                sender.sendMessage(ChatColor.GREEN + "/giftcode create <name> random - Tạo ngẫu nhiên 10 mã quà tặng");
                sender.sendMessage(ChatColor.GREEN + "/giftcode del <code> - Xóa mã quà tặng");
                sender.sendMessage(ChatColor.GREEN + "/giftcode reload - Tải lại pluins");
                sender.sendMessage(ChatColor.GREEN + "/giftcode enable <code> - Kích hoạt mã quà tặng");
                sender.sendMessage(ChatColor.GREEN + "/giftcode disable <code> - Vô hiệu hóa mã quà tặng");
                sender.sendMessage(ChatColor.GREEN + "/giftcode list - Danh sách mã quà tặng");
                sender.sendMessage(
                        ChatColor.GREEN + "/giftcode assign <code> <player> - Gán mã quà tặng cho cho người chơi ");
                sender.sendMessage(ChatColor.YELLOW + "Tác giả: QuangDev05");
                sender.sendMessage(ChatColor.YELLOW + "Phiên bản hiện tại: v" + getDescription().getVersion());
                return true;
            }

            if (!sender.hasPermission("giftcode.admin")) {
                sender.sendMessage(ChatColor.RED + "Bạn không có quyền xử dụng lệnh này.");
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "create":
                    if (args.length == 2) {
                        if (giftCodes.containsKey(args[1])) { // Kiểm tra mã quà tặng đã tồn tại
                            sender.sendMessage(
                                    ChatColor.RED + "Mã quà tặng " + args[1] + " đã tồn tại. Vui lòng chọn mã khác.");
                        } else {
                            createGiftCode(args[1], Collections.singletonList("give %player% diamond 1"),
                                    "Bạn đã nhận 1 viên kim cương!", 10, "2024-12-31T23:59:59", true, 1);
                            sender.sendMessage(ChatColor.GREEN + "Mã quà tặng " + args[1] + " tạo thành công!");
                        }
                    } else if (args.length == 3 && args[2].equalsIgnoreCase("random")) {
                        createRandomGiftCodes(args[1], 10);
                        sender.sendMessage(ChatColor.GREEN + "Tạo 10 mã quà tặng ngẫu nhiên với tên cơ sở " + args[1]);
                    } else {
                        sender.sendMessage(
                                ChatColor.RED + "Sử dụng: /giftcode create <code> hoặc /giftcode create <name> random");
                    }
                    break;
                case "del":
                    if (args.length == 2) {
                        deleteGiftCode(args[1]);
                        sender.sendMessage(ChatColor.GREEN + "Mã quà tặng " + args[1] + " đã bị xóa!");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Sử dụng: /giftcode del <code>");
                    }
                    break;
                case "reload":
                    reloadConfig(); // Nạp lại config.yml
                    createConfigFiles(); // Tạo lại các file cấu hình nếu cần
                    loadGiftCodes(); // Tải lại dữ liệu từ giftcode.yml
                    loadDataplayerConfig(); // Tải lại dữ liệu từ dataplayer.yml
                    sender.sendMessage(ChatColor.GREEN + "Đã tải lại tất cả các file cấu hình!");
                    break;
                case "enable":
                    if (args.length == 2) {
                        GiftCode codeToEnable = giftCodes.get(args[1]);
                        if (codeToEnable != null) {
                            codeToEnable.setEnabled(true);
                            saveGiftCodes();
                            sender.sendMessage(ChatColor.GREEN + "Mã quà tặng " + args[1] + " Đã kích hoạt!");
                        } else {
                            sender.sendMessage(ChatColor.RED + "Mã quà tặng không tồn tại!");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "Sử dụng: /giftcode enable <code>");
                    }
                    break;
                case "disable":
                    if (args.length == 2) {
                        GiftCode codeToDisable = giftCodes.get(args[1]);
                        if (codeToDisable != null) {
                            codeToDisable.setEnabled(false);
                            saveGiftCodes();
                            sender.sendMessage(ChatColor.GREEN + "Mã quà tặng " + args[1] + " Đã bị vô hiệu hóa!");
                        } else {
                            sender.sendMessage(ChatColor.RED + "Không tồn tài!");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "Sử dụng: /giftcode disable <code>");
                    }
                    break;
                case "list":
                    sender.sendMessage(ChatColor.YELLOW + "Danh sách mã quà tặng:");
                    for (String code : listGiftCodes()) {
                        sender.sendMessage(ChatColor.YELLOW + code);
                    }
                    break;
                    case "assign":
                    if (args.length == 3) { // args[0] là "assign", args[1] là mã quà tặng, args[2] là tên người chơi
                        String code = args[1];
                        Player targetPlayer = Bukkit.getPlayer(args[2]);
                        if (targetPlayer != null) {
                            assignGiftCodeToPlayer(sender, code, targetPlayer); // Gọi phương thức với biến sender
                            sender.sendMessage(ChatColor.GOLD + "Thất bại ⬆⬆⬆" + ChatColor.GRAY + " hoặc" + ChatColor.GREEN + " mã quà tặng " + code + " đã được gán cho "
                                    + targetPlayer.getName() + " thành công.");
                        } else {
                            sender.sendMessage(ChatColor.RED + "Không tìm thấy người chơi!");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "Sử dụng: /giftcode assign <code> <player>");
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
                    // Kiểm tra thời gian hết hạn
                    if (!giftCode.getExpiry().isEmpty()) {
                        try {
                            Date expiryDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(giftCode.getExpiry());
                            if (new Date().after(expiryDate)) {
                                player.sendMessage(ChatColor.RED + getConfig().getString("messages.code-expired"));
                                return true;
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                            player.sendMessage(ChatColor.RED + "Đã xảy ra lỗi khi kiểm tra thời gian hết hạn.");
                            return true;
                        }
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
                    if (playerMaxUses == -1) {
                        // Không giới hạn
                    } else {
                        if (checkPlayerHasUsedCode(player, code)) {
                            player.sendMessage(ChatColor.RED + getConfig().getString("messages.code-already-redeemed"));
                            return true;
                        }
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
                    player.sendMessage(ChatColor.RED + "Sử dụng: /code <code>");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Chỉ người chơi mới có thể sử dụng lệnh này.");
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

        public GiftCode(List<String> commands, String message, int maxUses, String expiry, boolean enabled,
                int playerMaxUses) {
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