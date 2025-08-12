package quangdev05.giftcode24.update;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*; // Set, UUID, Collections, ConcurrentHashMap

import quangdev05.giftcode24.GiftCode24;

public class UpdateChecker implements Listener {
    private final JavaPlugin plugin;

    // Người chơi đã được báo trong lần chạy server này (tránh spam)
    private final Set<UUID> notifiedOnce = Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());

    private boolean consoleReminderStarted = false; // chỉ tạo timer 1 lần

    public UpdateChecker(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void checkLatestReleaseAsync() {
        boolean checkUpdate = plugin.getConfig().getBoolean("check-update", true);
        if (!checkUpdate) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    String url = "https://api.github.com/repos/quangdev05/GiftCode24/releases/latest";
                    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) response.append(line);
                        reader.close();
                        String json = response.toString();

                        String latest = null;
                        String key = "\"tag_name\":\"";
                        int idx = json.indexOf(key);
                        if (idx >= 0) {
                            int start = idx + key.length();
                            int end = json.indexOf('\"', start);
                            if (end > start) latest = json.substring(start, end);
                        }

                        if (latest != null && plugin instanceof GiftCode24) {
                            GiftCode24 gc = (GiftCode24) plugin;
                            gc.setLatestVersion(latest);

                            // Log 1 phát ngay khi fetch được
                            if (!latest.equals(plugin.getDescription().getVersion())) {
                                plugin.getLogger().info("[GiftCode24] Update available: v" + latest +
                                        " (current v" + plugin.getDescription().getVersion() + ")");
                            }

                            // Khởi động nhắc console mỗi 9 phút (chỉ 1 lần)
                            startConsoleReminder();
                        }
                    }
                } catch (Exception ignored) {}
            }
        }.runTaskAsynchronously(plugin);
    }

    // Nhắc console mỗi 9 phút nếu có bản mới
    private void startConsoleReminder() {
        if (consoleReminderStarted) return;
        consoleReminderStarted = true;

        final long PERIOD = 9L * 60L * 20L; // 9 phút
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!(plugin instanceof GiftCode24)) return;
                GiftCode24 gc = (GiftCode24) plugin;
                String latest = gc.getLatestVersion();
                if (latest == null) return;

                String current = plugin.getDescription().getVersion();
                if (!latest.equalsIgnoreCase(current)) {
                    plugin.getLogger().info("Update available: v" + latest +
                            " (current v" + current + ")");
                }
            }
        }.runTaskTimer(plugin, PERIOD, PERIOD);
    }

    // Báo 1 lần cho từng admin khi họ join (nếu có bản mới)
    @EventHandler
    public void onAdminJoin(PlayerJoinEvent e) {
        if (!(plugin instanceof GiftCode24)) return;
        GiftCode24 gc = (GiftCode24) plugin;

        String latest = gc.getLatestVersion();
        if (latest == null) return;

        String current = plugin.getDescription().getVersion();
        if (latest.equalsIgnoreCase(current)) return;

        // Chỉ gửi cho admin (quyền giftcode.admin) và chỉ 1 lần/người trong phiên
        var p = e.getPlayer();
        if (!p.hasPermission("giftcode.admin")) return;
        if (!notifiedOnce.add(p.getUniqueId())) return;

        p.sendMessage(ChatColor.YELLOW + "[GiftCode24] A new version is available: "
                + ChatColor.GOLD + "v" + latest + ChatColor.YELLOW
                + " (current " + ChatColor.GRAY + "v" + current + ChatColor.YELLOW + ").");
    }
}