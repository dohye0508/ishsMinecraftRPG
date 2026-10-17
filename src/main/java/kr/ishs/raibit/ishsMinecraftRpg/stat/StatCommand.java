package kr.ishs.raibit.ishsMinecraftRpg.stat;

import kr.ishs.raibit.ishsMinecraftRpg.config.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            StatGUI.open(player);
        } else {
            Messages.send(sender, "common.players-only");
        }
        return true;
    }
}
