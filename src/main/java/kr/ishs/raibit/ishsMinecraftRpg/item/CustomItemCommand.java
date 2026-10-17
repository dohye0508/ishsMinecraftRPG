package kr.ishs.raibit.ishsMinecraftRpg.item;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CustomItemCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && !sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "권한이 없습니다.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.YELLOW + "사용법: /아이템지급 [플레이어] [아이템ID]");
            sender.sendMessage(ChatColor.GRAY + "아이템ID: " + String.join(", ", CustomItems.getIds()));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "플레이어를 찾을 수 없습니다: " + args[0]);
            return true;
        }

        ItemStack item = CustomItems.create(args[1]);
        if (item == null) {
            sender.sendMessage(ChatColor.RED + "존재하지 않는 아이템ID입니다: " + args[1]);
            sender.sendMessage(ChatColor.GRAY + "아이템ID: " + String.join(", ", CustomItems.getIds()));
            return true;
        }

        target.getInventory().addItem(item);
        target.sendMessage(ChatColor.GREEN + "[시스템] " + item.getItemMeta().getDisplayName() + ChatColor.GREEN + " 아이템을 지급받았습니다!");
        sender.sendMessage(ChatColor.GREEN + target.getName() + "님에게 " + item.getItemMeta().getDisplayName() + ChatColor.GREEN + " 아이템을 지급했습니다.");
        return true;
    }
}
