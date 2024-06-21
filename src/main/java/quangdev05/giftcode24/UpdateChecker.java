package quangdev05.giftcode24;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker {

    private final JavaPlugin plugin;
    private static final String CHECK_URL = "https://www.doithe24.net/?check-update";
    private static final String DOWNLOAD_URL = "https://www.spigotmc.org/resources/giftcode24.117453/";

    public UpdateChecker(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void checkForUpdates() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(CHECK_URL).openConnection();
                connection.setRequestMethod("GET");
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String latestVersion = reader.readLine().trim();
                reader.close();

                if (!plugin.getDescription().getVersion().equalsIgnoreCase(latestVersion)) {
                    String updateMessage = ChatColor.YELLOW + "[GiftCode24] A new update is available: " + latestVersion +
                            "\nDownload it here: " + DOWNLOAD_URL;

                    // Gửi tin nhắn đến console
                    plugin.getServer().getConsoleSender().sendMessage(updateMessage);

                    // Gửi tin nhắn đến tất cả người chơi có quyền giftcode.admin
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.hasPermission("giftcode.admin")) {
                            player.sendMessage(updateMessage);
                        }
                    }
                }
            } catch (Exception e) {
                String errorMessage = ChatColor.RED + "[GiftCode24] Failed to check for updates.";
                plugin.getServer().getConsoleSender().sendMessage(errorMessage);
                e.printStackTrace();
            }
        });
    }
}
