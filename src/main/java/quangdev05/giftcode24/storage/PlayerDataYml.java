package quangdev05.giftcode24.storage;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataYml {

    private final JavaPlugin plugin;
    private final File file;

    /** Cache in-memory thread-safe */
    private final ConcurrentHashMap<UUID, PlayerData> playerData = new ConcurrentHashMap<>();

    /** Snapshot YAML để build section khi cần (phục vụ getPlayersSection() legacy) */
    private volatile YamlConfiguration lastSnapshot = new YamlConfiguration();

    public PlayerDataYml(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "dataplayer.yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource("dataplayer.yml", false);
        }
        reload(); // nạp vào map
    }

    /** Model dữ liệu một player */
    private static final class PlayerData {
        String ip = "";
        final List<String> usedCodes = new ArrayList<>();
        final List<String> assignedCodes = new ArrayList<>();
    }

    /* =========================
       Load / Save
       ========================= */

    /** Đọc YAML -> ConcurrentHashMap */
    public void reload() {
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        playerData.clear();

        ConfigurationSection playersSec = cfg.getConfigurationSection("players");
        if (playersSec != null) {
            for (String key : playersSec.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    ConfigurationSection ps = playersSec.getConfigurationSection(key);
                    if (ps == null) continue;

                    PlayerData pd = new PlayerData();
                    pd.ip = ps.getString("ip", "");
                    pd.usedCodes.addAll(ps.getStringList("usedCodes"));
                    pd.assignedCodes.addAll(ps.getStringList("assignedCodes"));

                    playerData.put(uuid, pd);
                } catch (IllegalArgumentException ignored) {
                    // skip invalid uuid key
                }
            }
        }
        // lưu snapshot mới cho getPlayersSection()
        lastSnapshot = cfg;
    }

    /** Ghi ConcurrentHashMap -> YAML */
    public synchronized void save() {
        YamlConfiguration out = new YamlConfiguration();
        for (Map.Entry<UUID, PlayerData> e : playerData.entrySet()) {
            String base = "players." + e.getKey();
            PlayerData pd = e.getValue();
            out.set(base + ".ip", pd.ip);
            out.set(base + ".usedCodes", new ArrayList<>(pd.usedCodes));
            out.set(base + ".assignedCodes", new ArrayList<>(pd.assignedCodes));
        }
        try {
            out.save(file);
            lastSnapshot = out; // cập nhật snapshot sau khi lưu
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /* =========================
       Helpers
       ========================= */

    private PlayerData ensure(UUID uuid) {
        return playerData.computeIfAbsent(uuid, k -> new PlayerData());
    }

    /* =========================
       API cũ giữ nguyên hành vi
       ========================= */

    public List<String> getUsedCodes(UUID uuid) {
        return new ArrayList<>(ensure(uuid).usedCodes);
    }

    public void addUsedCode(Player player, String code) {
        UUID uuid = player.getUniqueId();
        PlayerData pd = ensure(uuid);
        // cập nhật IP hiện tại của player (nếu có)
        try {
            String ip = player.getAddress() != null && player.getAddress().getAddress() != null
                    ? player.getAddress().getAddress().getHostAddress()
                    : "";
            pd.ip = ip;
        } catch (Exception ignored) {}

        pd.usedCodes.add(code);
        save();
    }

    public String getPlayerIP(UUID uuid) {
        return ensure(uuid).ip;
    }

    public List<String> getAssignedCodes(UUID uuid) {
        return new ArrayList<>(ensure(uuid).assignedCodes);
    }

    public void addAssignedCode(UUID uuid, String code) {
        ensure(uuid).assignedCodes.add(code);
        save();
    }

    public ConfigurationSection getPlayersSection() {
        // Build tạm thời YAML từ map hiện tại để trả về section mới nhất
        YamlConfiguration cfg = new YamlConfiguration();
        for (Map.Entry<UUID, PlayerData> e : playerData.entrySet()) {
            String base = "players." + e.getKey();
            PlayerData pd = e.getValue();
            cfg.set(base + ".ip", pd.ip);
            cfg.set(base + ".usedCodes", new ArrayList<>(pd.usedCodes));
            cfg.set(base + ".assignedCodes", new ArrayList<>(pd.assignedCodes));
        }
        // Không cần save ra file; chỉ để đọc in-memory
        return cfg.getConfigurationSection("players");
    }

    /* =========================
       Các tiện ích duyệt nhanh (không bắt buộc)
       ========================= */

    /** Duyệt toàn bộ dữ liệu players theo map in-memory (tối ưu hơn section) */
    public Set<Map.Entry<UUID, PlayerData>> entrySet() {
        return playerData.entrySet();
    }
}