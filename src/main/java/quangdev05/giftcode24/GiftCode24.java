package quangdev05.giftcode24;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;

import quangdev05.giftcode24.commands.GiftCodeAdminCommand;
import quangdev05.giftcode24.commands.RedeemCodeCommand;
import quangdev05.giftcode24.gui.GiftCodeListGUI;
import quangdev05.giftcode24.gui.GiftItemEditorGUI;
import quangdev05.giftcode24.manager.GiftCodeManager;
import quangdev05.giftcode24.storage.GiftCodesYml;
import quangdev05.giftcode24.storage.PlayerDataYml;
import quangdev05.giftcode24.update.UpdateChecker;

public class GiftCode24 extends JavaPlugin implements Listener {

    private GiftCodesYml giftCodesYml;
    private PlayerDataYml playerDataYml;
    private GiftCodeManager giftCodeManager;
    private GiftCodeListGUI giftCodeListGUI;
    private GiftItemEditorGUI giftItemEditorGUI;
    private UpdateChecker updateChecker;

    private volatile String latestVersion;
    public String getLatestVersion() { return latestVersion; }
    public void setLatestVersion(String v) { this.latestVersion = v; }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Storage
        this.giftCodesYml = new GiftCodesYml(this);
        this.playerDataYml = new PlayerDataYml(this);

        // Manager
        this.giftCodeManager = new GiftCodeManager(this, giftCodesYml, playerDataYml);

        // GUI
        this.giftCodeListGUI = new GiftCodeListGUI(this, giftCodeManager);
        this.giftItemEditorGUI = new GiftItemEditorGUI(giftCodeManager);
        getServer().getPluginManager().registerEvents(giftCodeListGUI, this);
        getServer().getPluginManager().registerEvents(giftItemEditorGUI, this);


        // Commands
        GiftCodeAdminCommand adminCmd = new GiftCodeAdminCommand(this, giftCodeManager, giftCodeListGUI);
        getCommand("giftcode").setExecutor(adminCmd);
        getCommand("giftcode").setTabCompleter(adminCmd);

        RedeemCodeCommand redeemCmd = new RedeemCodeCommand(this, giftCodeManager, playerDataYml);
        getCommand("code").setExecutor(redeemCmd);

        sendFancyMessage();

        // Update check (async)
        this.updateChecker = new UpdateChecker(this);
        getServer().getPluginManager().registerEvents(updateChecker, this);
        updateChecker.checkLatestReleaseAsync();

        // bStats (keep plugin id from legacy)
        try {
            Class<?> metricsClass = Class.forName("org.bstats.bukkit.Metrics");
            Object metrics = metricsClass
                    .getConstructor(JavaPlugin.class, int.class)
                    .newInstance(this, 24198);
        } catch (Exception ignored) { }
    }

    @Override
    public void onDisable() {
        // Persist current codes to file
        giftCodesYml.saveAll(giftCodeManager.getAll());
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

    public GiftCodeManager getGiftCodeManager() {
        return giftCodeManager;
    }

    public PlayerDataYml getPlayerDataYml() {
        return playerDataYml;
    }

    public GiftCodesYml getGiftCodesYml() {
        return giftCodesYml;
    }

    public GiftItemEditorGUI getGiftItemEditorGUI() { return giftItemEditorGUI; }
}
