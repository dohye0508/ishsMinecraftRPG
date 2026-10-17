package kr.ishs.raibit.ishsMinecraftRpg.item;

import kr.ishs.raibit.ishsMinecraftRpg.config.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CustomItemCommand implements CommandExecutor {

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && !sender.isOp()) {
            Messages.send(sender, "common.no-permission");
            return true;
        }

        if (args.length < 2) {
            Messages.send(sender, "item.usage");
            Messages.send(sender, "item.id-list", "ids", String.join(", ", CustomItems.getIds()));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            Messages.send(sender, "item.player-not-found", "player", args[0]);
            return true;
        }

        ItemStack item = CustomItems.create(args[1]);
        if (item == null) {
            Messages.send(sender, "item.unknown-item", "id", args[1]);
            Messages.send(sender, "item.id-list", "ids", String.join(", ", CustomItems.getIds()));
            return true;
        }

        String itemName = item.getItemMeta().getDisplayName();
        target.getInventory().addItem(item);
        Messages.send(target, "item.received", "item", itemName);
        Messages.send(sender, "item.given", "player", target.getName(), "item", itemName);
        return true;
    }
}
