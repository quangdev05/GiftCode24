package quangdev05.giftcode24.storage;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlayerDataYml {
    private final JavaPlugin plugin;
    private final File file;
    private FileConfiguration cfg;

    public PlayerDataYml(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "dataplayer.yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource("dataplayer.yml", false);
        }
        this.cfg = YamlConfiguration.loadConfiguration(file);
    }

    public void reload() {
        this.cfg = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try { cfg.save(file); } catch (IOException e) { e.printStackTrace(); }
    }

    public List<String> getUsedCodes(UUID uuid) {
        return new ArrayList<>(cfg.getStringList("players." + uuid + ".usedCodes"));
    }

    public void addUsedCode(Player player, String code) {
        UUID uuid = player.getUniqueId();
        String ip = player.getAddress().getAddress().getHostAddress();
        List<String> used = getUsedCodes(uuid);
        used.add(code);
        cfg.set("players." + uuid + ".ip", ip);
        cfg.set("players." + uuid + ".usedCodes", used);
        save();
    }

    public String getPlayerIP(UUID uuid) {
        return cfg.getString("players." + uuid + ".ip");
    }

    public List<String> getAssignedCodes(UUID uuid) {
        List<String> list = cfg.getStringList("players." + uuid + ".assignedCodes");
        return list == null ? new ArrayList<>() : new ArrayList<>(list);
    }

    public void addAssignedCode(UUID uuid, String code) {
        List<String> list = getAssignedCodes(uuid);
        list.add(code);
        cfg.set("players." + uuid + ".assignedCodes", list);
        save();
    }

    public ConfigurationSection getPlayersSection() {
        return cfg.getConfigurationSection("players");
    }
}
