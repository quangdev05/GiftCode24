package quangdev05.giftcode24.manager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import quangdev05.giftcode24.GiftCode24;
import quangdev05.giftcode24.model.GiftCode;
import quangdev05.giftcode24.storage.GiftCodesYml;
import quangdev05.giftcode24.storage.PlayerDataYml;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.security.SecureRandom;

public class GiftCodeManager {

    private final GiftCode24 plugin;
    private final GiftCodesYml giftCodesYml;
    private final PlayerDataYml playerDataYml;
    private final Map<String, GiftCode> giftCodes;
    private static final String RANDOM_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ0123456789";
    private final SecureRandom rng = new SecureRandom();

    private String randomSuffix(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(RANDOM_CHARS.charAt(rng.nextInt(RANDOM_CHARS.length())));
        return sb.toString();
    }

    public GiftCodeManager(GiftCode24 plugin, GiftCodesYml giftCodesYml, PlayerDataYml playerDataYml) {
        this.plugin = plugin;
        this.giftCodesYml = giftCodesYml;
        this.playerDataYml = playerDataYml;
        this.giftCodes = new LinkedHashMap<>(giftCodesYml.loadAll());
    }

    public Map<String, GiftCode> getAll() {
        return giftCodes;
    }

    public boolean exists(String code) {
        return giftCodes.containsKey(code);
    }

    public GiftCode getGiftCode(String code) {
        return giftCodes.get(code);
    }

    public GiftCode get(String code) {
        return giftCodes.get(code);
    }

    public void save() {
        giftCodesYml.saveAll(giftCodes);
    }

    public void createGiftCode(String code, List<String> commands, List<String> message, int maxUses, String expiry,
                               boolean enabled, int playerMaxUses, int maxUsesPerIP, int requiredPlaytime) {
        if (giftCodes.containsKey(code)) {
            plugin.getLogger().warning("Gift code \"" + code + "\" already exists. Please create another code.");
            return;
        }
        GiftCode giftCode = new GiftCode(
                commands, message, maxUses, expiry, enabled,
                playerMaxUses, maxUsesPerIP, requiredPlaytime,
                new java.util.ArrayList<org.bukkit.inventory.ItemStack>() // items mặc định rỗng
        );
        giftCodes.put(code, giftCode);
        save();
    }

    public java.util.List<ItemStack> getItemsForCode(String code) {
        GiftCode gc = giftCodes.get(code);
        return gc != null ? new java.util.ArrayList<>(gc.getItemRewards()) : new java.util.ArrayList<>();
    }

    public void setItemsForCode(String code, java.util.List<ItemStack> items) {
        GiftCode gc = giftCodes.get(code);
        if (gc == null) return;
        gc.setItemRewards(items);
        save();
    }

    public void deleteGiftCode(String code) {
        giftCodes.remove(code);
        save();
    }

    public void reloadFromDisk() {
        giftCodesYml.reload();
        playerDataYml.reload();
        this.giftCodes.clear();
        this.giftCodes.putAll(giftCodesYml.loadAll());
    }

    public List<String> listGiftCodes() {
        return new ArrayList<>(giftCodes.keySet());
    }

    public void assignGiftCodeToPlayer(CommandSender sender, String code, Player player) {
        GiftCode gc = giftCodes.get(code);
        if (gc == null) {
            sender.sendMessage(ChatColor.RED + "Gift code \"" + ChatColor.YELLOW + code + ChatColor.RED + "\" does not exist!");
            return;
        }

        // Ghi dấu đã assign
        playerDataYml.addAssignedCode(player.getUniqueId(), code);

        // Chuẩn bị dữ liệu trước khi schedule (tránh concurrent modification)
        final List<String> cmds = new ArrayList<>(gc.getCommands());
        final List<String> msgs = new ArrayList<>(gc.getMessages());
        final List<ItemStack> items = gc.getItemRewards() != null ? new ArrayList<>(gc.getItemRewards()) : Collections.emptyList();

        // 1) Chạy command bằng GlobalRegionScheduler (chuẩn Folia cho console)
        Bukkit.getGlobalRegionScheduler().execute(plugin, () -> {
            for (String cmd : cmds) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
            }
        });

        // 2) Phát item + gửi tin nhắn trên thread vùng của player
        player.getScheduler().run(plugin, task -> {
            // Phát item
            if (!items.isEmpty()) {
                for (ItemStack it : items) {
                    if (it == null) continue;
                    Map<Integer, ItemStack> leftovers = player.getInventory().addItem(it.clone());
                    if (!leftovers.isEmpty()) {
                        for (ItemStack lf : leftovers.values()) {
                            if (lf == null) continue;
                            player.getWorld().dropItemNaturally(player.getLocation(), lf);
                        }
                    }
                }
            }

            // Gửi messages
            player.sendMessage(ChatColor.GREEN + "You have been assigned a gift code by the administrator.");
            for (String msg : msgs) {
                player.sendMessage(ChatColor.GREEN + msg);
            }
        }, null);

        // 3) Phản hồi admin
        sender.sendMessage(ChatColor.GREEN + "Assigned gift code " + ChatColor.YELLOW + code
                + ChatColor.GREEN + " to " + ChatColor.AQUA + player.getName() + ChatColor.GREEN + ".");
    }

    public int createRandomGiftCodes(String base, int amount) {
        int created = 0;
        if (amount <= 0) return 0;

        for (int i = 0; i < amount; i++) {
            String code;
            int tries = 0;
            do {
                code = base + randomSuffix(8);   // ví dụ: EVENT- + 8 ký tự
                tries++;
            } while (giftCodes.containsKey(code) && tries < 50);
            if (giftCodes.containsKey(code)) continue;

            GiftCode gc = new GiftCode(
                    java.util.Collections.singletonList("give %player% diamond 1"),
                    java.util.Collections.singletonList("You have received 1 diamond!"),
                    99,
                    "2029-12-31T23:59:59",
                    true,
                    1,
                    1,
                    8,
                    new java.util.ArrayList<ItemStack>()
            );
            giftCodes.put(code, gc);
            created++;
        }
        save();
        return created;
    }

    public int createRandomGiftCodesFromTemplate(String base, int amount, String templateCode) {
        GiftCode t = giftCodes.get(templateCode);
        if (t == null) return -1; // báo không có template

        int created = 0;
        if (amount <= 0) return 0;

        // clone nội dung từ template
        java.util.List<String> cmds = new java.util.ArrayList<>(t.getCommands());
        java.util.List<String> msgs = new java.util.ArrayList<>(t.getMessages());
        java.util.List<ItemStack> items = new java.util.ArrayList<>();
        if (t.getItemRewards() != null) {
            for (ItemStack it : t.getItemRewards()) {
                if (it != null) items.add(it.clone()); // giữ NBT
            }
        }

        for (int i = 0; i < amount; i++) {
            String code;
            int tries = 0;
            do {
                code = base + randomSuffix(8);
                tries++;
            } while (giftCodes.containsKey(code) && tries < 50);
            if (giftCodes.containsKey(code)) continue; // đụng hàng quá nhiều thì bỏ lượt này

            // mỗi code random dùng 1 lần, các ràng buộc khác lấy từ template để đồng bộ hành vi
            GiftCode gc = new GiftCode(
                    cmds,
                    msgs,
                    99,
                    t.getExpiry(),              // copy hạn dùng
                    true,            // copy trạng thái enable
                    t.getPlayerMaxUses(),       // copy limit theo người
                    t.getMaxUsesPerIP(),        // copy limit theo IP
                    t.getRequiredPlaytime(),    // copy yêu cầu playtime
                    items                       // copy item rewards (clone)
            );

            giftCodes.put(code, gc);
            created++;
        }
        save();
        return created;
    }

    public boolean checkPlayerHasUsedCode(Player player, String code) {
        List<String> usedCodes = playerDataYml.getUsedCodes(player.getUniqueId());
        int playerMaxUses = getPlayerMaxUsesForCode(code);
        if (playerMaxUses == -1) return false;
        return Collections.frequency(usedCodes, code) >= playerMaxUses;
    }

    public void addPlayerUsedCode(Player player, String code) {
        playerDataYml.addUsedCode(player, code);
    }

    public int getPlayerMaxUsesForCode(String code) {
        if (giftCodes.containsKey(code)) {
            return giftCodes.get(code).getPlayerMaxUses();
        }
        return 1;
    }

    public int calculateUsedCount(String code) {
        int count = 0;
        if (playerDataYml.getPlayersSection() != null) {
            for (String key : playerDataYml.getPlayersSection().getKeys(false)) {
                List<String> usedCodes = playerDataYml.getPlayersSection().getStringList(key + ".usedCodes");
                count += Collections.frequency(usedCodes, code);
            }
        }
        return count;
    }

    public String redeem(Player player, String code) {
        GiftCode giftCode = giftCodes.get(code);
        if (giftCode == null)
            return ChatColor.RED + plugin.getConfig().getString("messages.invalid-code", "The gift code you entered is invalid.");

        if (!giftCode.isEnabled())
            return ChatColor.RED + plugin.getConfig().getString("messages.code-disabled", "The gift code you entered is currently disabled.");

        String requiredPerm = giftCode.getPermission();
        if (requiredPerm != null && !requiredPerm.isEmpty() && !player.hasPermission(requiredPerm)) {
            return ChatColor.RED + plugin.getConfig().getString("messages.no-permission-code",
                    "You don't have permission to use this code.");
        }

        if (giftCode.getRequiredPlaytime() > 0) {
            int playerPlaytime = player.getStatistic(Statistic.PLAY_ONE_MINUTE) / (20 * 60);
            if (playerPlaytime < giftCode.getRequiredPlaytime()) {
                String message = plugin.getConfig().getString("messages.not-enough-playtime", "You need to play for at least {required} minutes to use this code. You have currently played for {current} minutes.")
                        .replace("{required}", String.valueOf(giftCode.getRequiredPlaytime()))
                        .replace("{current}", String.valueOf(playerPlaytime));
                return ChatColor.RED + message;
            }
        }

        if (giftCode.getExpiry() != null && !giftCode.getExpiry().isEmpty()) {
            try {
                Date expiryDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(giftCode.getExpiry());
                if (new Date().after(expiryDate)) {
                    return ChatColor.RED + plugin.getConfig().getString("messages.code-expired", "The gift code you entered has expired.");
                }
            } catch (ParseException e) {
                e.printStackTrace();
                return ChatColor.RED + "An error occurred while checking the expiration time.";
            }
        }

        if (giftCode.getMaxUses() <= 0) {
            return ChatColor.RED + plugin.getConfig().getString("messages.max-uses-reached", "The gift code has reached its limit.");
        }

        if (giftCode.getMaxUsesPerIP() > 0) {
            String playerIP = player.getAddress().getAddress().getHostAddress();
            List<String> usedCodesByIP = new ArrayList<>();
            if (playerDataYml.getPlayersSection() != null) {
                for (String uuid : playerDataYml.getPlayersSection().getKeys(false)) {
                    String ip = playerDataYml.getPlayersSection().getString(uuid + ".ip");
                    if (playerIP.equals(ip)) {
                        usedCodesByIP.addAll(playerDataYml.getPlayersSection().getStringList(uuid + ".usedCodes"));
                    }
                }
            }
            int ipUsageCount = Collections.frequency(usedCodesByIP, code);
            if (ipUsageCount >= giftCode.getMaxUsesPerIP()) {
                return ChatColor.RED + plugin.getConfig().getString("messages.max-uses-perip", "This gift code has been used more times than allowed from your IP address.");
            }
        }

        if (checkPlayerHasUsedCode(player, code)) {
            return ChatColor.RED + plugin.getConfig().getString("messages.code-already-redeemed", "You have entered this code more than the specified number of times.");
        }

        // --- Chuẩn bị dữ liệu (tránh sửa list gốc hoặc đụng thread-safety) ---
        final List<String> cmds = new ArrayList<>(giftCode.getCommands());
        final List<String> msgs = new ArrayList<>(giftCode.getMessages());
        final List<ItemStack> items = giftCode.getItemRewards() != null ? new ArrayList<>(giftCode.getItemRewards()) : Collections.emptyList();

        // 1) Chạy command console trên Global thread (Folia bắt buộc)
        Bukkit.getGlobalRegionScheduler().execute(plugin, () -> {
            for (String cmd : cmds) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
            }
        });

        // 2) Phát item + gửi tin nhắn + cập nhật save trên thread vùng của player
        player.getScheduler().run(plugin, task -> {
            // Phát item
            if (!items.isEmpty()) {
                for (ItemStack it : items) {
                    if (it == null) continue;
                    Map<Integer, ItemStack> leftovers = player.getInventory().addItem(it.clone());
                    if (!leftovers.isEmpty()) {
                        for (ItemStack lf : leftovers.values()) {
                            if (lf == null) continue;
                            player.getWorld().dropItemNaturally(player.getLocation(), lf);
                        }
                    }
                }
            }

            // Gửi messages
            for (String msg : msgs) {
                player.sendMessage(ChatColor.GREEN + msg);
            }

            // Update đếm dùng, giảm maxUses và lưu
            giftCode.setMaxUses(giftCode.getMaxUses() - 1);
            addPlayerUsedCode(player, code);
            save(); // ghi YAML; nếu muốn không block thì chuyển sang AsyncScheduler
        }, null);

        // 3) Trả thông điệp thành công ngay cho người gọi lệnh
        return ChatColor.GREEN + plugin.getConfig().getString("messages.code-redeemed", "You have successfully redeemed your gift code!");
    }
}