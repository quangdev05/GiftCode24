package quangdev05.giftcode24.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import quangdev05.giftcode24.GiftCode24;
import quangdev05.giftcode24.manager.GiftCodeManager;
import quangdev05.giftcode24.storage.PlayerDataYml;

public class RedeemCodeCommand implements CommandExecutor {

    private final GiftCode24 plugin;
    private final GiftCodeManager manager;
    private final PlayerDataYml playerData;

    public RedeemCodeCommand(GiftCode24 plugin, GiftCodeManager manager, PlayerDataYml playerData) {
        this.plugin = plugin;
        this.manager = manager;
        this.playerData = playerData;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /code <code>");
            return true;
        }
        String result = manager.redeem(player, args[0]);
        player.sendMessage(result);
        return true;
    }
}
